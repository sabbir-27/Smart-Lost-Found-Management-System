package com.smartlostfound.storage;

import com.smartlostfound.model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust CSV implementation of {@link ItemStorageService}.
 * Implements RFC-4180 compliant parsing and serialization with support for
 * quoted strings, embedded commas, and multiline descriptions.
 * Handles polymorphic persistence of {@link LostItem} and {@link FoundItem}.
 */
public class CsvItemStorageService implements ItemStorageService {
    private static final String CSV_HEADER = 
            "TYPE,ID,NAME,CATEGORY,DATE,DESCRIPTION,STATUS,LOCATION,SPECIFIC_1,SPECIFIC_2,REPORTER_NAME,REPORTER_EMAIL,REPORTER_PHONE,RESOLUTION_NOTES,CREATED_AT";

    private final Path filePath;

    /**
     * Constructs a CsvItemStorageService targeting the given file path.
     *
     * @param filePath Path to the CSV storage file
     */
    public CsvItemStorageService(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Constructs a CsvItemStorageService targeting a file path string.
     *
     * @param pathString File system path string
     */
    public CsvItemStorageService(String pathString) {
        this(Paths.get(pathString));
    }

    @Override
    public synchronized List<Item> loadItems() throws DataPersistenceException {
        List<Item> items = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return items;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;
            StringBuilder multiLineBuffer = new StringBuilder();
            boolean insideQuotes = false;

            while ((line = reader.readLine()) != null) {
                if (insideQuotes) {
                    multiLineBuffer.append("\n").append(line);
                } else {
                    multiLineBuffer = new StringBuilder(line);
                }

                insideQuotes = hasUnbalancedQuotes(multiLineBuffer.toString());
                if (insideQuotes) {
                    continue; // Wait for closing quote on subsequent lines
                }

                String completeRecord = multiLineBuffer.toString().trim();
                if (completeRecord.isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    // Skip header line if detected
                    if (completeRecord.toUpperCase().startsWith("TYPE,")) {
                        continue;
                    }
                }

                try {
                    Item item = parseCsvRecord(completeRecord);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping corrupted CSV record: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to read CSV storage file: " + filePath, e);
        }

        return items;
    }

    @Override
    public synchronized void saveItems(List<Item> items) throws DataPersistenceException {
        try {
            if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
                Files.createDirectories(filePath.getParent());
            }

            // Write to a temporary file first for atomic swap safety
            Path tempFile = Files.createTempFile(
                    filePath.getParent() != null ? filePath.getParent() : Paths.get("."),
                    "items_tmp_", ".csv");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (Item item : items) {
                    writer.write(serializeItemToCsv(item));
                    writer.newLine();
                }
            }

            // Atomically replace target file
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to persist items to CSV file: " + filePath, e);
        }
    }

    @Override
    public String getStoragePath() {
        return filePath.toAbsolutePath().toString();
    }

    @Override
    public boolean storageExists() {
        return Files.exists(filePath);
    }

    /**
     * Serializes an Item into an RFC-4180 compliant CSV line.
     * Uses polymorphism to serialize subclass specific fields.
     */
    private String serializeItemToCsv(Item item) {
        String type = (item instanceof LostItem) ? "LOST" : "FOUND";
        String id = item.getId();
        String name = item.getName();
        String category = item.getCategory().name();
        String date = item.getFormattedDate();
        String description = item.getDescription();
        String status = item.getStatus().name();
        String location = item.getSpecificLocation();
        String specific1 = item.getSpecificField1();
        String specific2 = item.getSpecificField2();

        User reporter = item.getReporter();
        String reporterName = (reporter != null) ? reporter.getName() : "";
        String reporterEmail = (reporter != null) ? reporter.getEmail() : "";
        String reporterPhone = (reporter != null) ? reporter.getPhone() : "";
        String resolutionNotes = item.getResolutionNotes();
        String createdAt = item.getFormattedCreatedAt();

        return String.join(",",
                escapeCsv(type),
                escapeCsv(id),
                escapeCsv(name),
                escapeCsv(category),
                escapeCsv(date),
                escapeCsv(description),
                escapeCsv(status),
                escapeCsv(location),
                escapeCsv(specific1),
                escapeCsv(specific2),
                escapeCsv(reporterName),
                escapeCsv(reporterEmail),
                escapeCsv(reporterPhone),
                escapeCsv(resolutionNotes),
                escapeCsv(createdAt)
        );
    }

    /**
     * Parses a single CSV line into an Item instance.
     */
    private Item parseCsvRecord(String line) {
        List<String> fields = parseCsvFields(line);
        if (fields.size() < 13) {
            throw new IllegalArgumentException("Insufficient fields in CSV row. Expected >= 13, found " + fields.size());
        }

        String type = fields.get(0).trim().toUpperCase();
        String id = fields.get(1).trim();
        String name = fields.get(2).trim();
        ItemCategory category = ItemCategory.fromString(fields.get(3));

        LocalDate date;
        try {
            date = LocalDate.parse(fields.get(4).trim(), Item.DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            date = LocalDate.now();
        }

        String description = fields.get(5).trim();
        ItemStatus status = ItemStatus.fromString(fields.get(6));
        String location = fields.get(7).trim();
        String specific1 = fields.get(8).trim();
        String specific2 = fields.get(9).trim();

        String reporterName = fields.get(10).trim();
        String reporterEmail = fields.get(11).trim();
        String reporterPhone = fields.get(12).trim();
        User reporter = new User(reporterName, reporterEmail, reporterPhone);

        String resolutionNotes = fields.size() > 13 ? fields.get(13).trim() : "";
        LocalDateTime createdAt = LocalDateTime.now();
        if (fields.size() > 14 && !fields.get(14).trim().isEmpty()) {
            try {
                createdAt = LocalDateTime.parse(fields.get(14).trim(), Item.DATETIME_FORMATTER);
            } catch (DateTimeParseException ignored) {
            }
        }

        Item item;
        if ("LOST".equalsIgnoreCase(type)) {
            double reward = 0.0;
            if (!specific1.isEmpty()) {
                try {
                    reward = Double.parseDouble(specific1);
                } catch (NumberFormatException ignored) {
                }
            }
            item = new LostItem(id, name, category, date, description, status, reporter, location, reward);
        } else {
            item = new FoundItem(id, name, category, date, description, status, reporter,
                    location, specific1, specific2);
        }

        item.setResolutionNotes(resolutionNotes);
        item.setCreatedAt(createdAt);
        return item;
    }

    /**
     * Escapes a string for CSV according to RFC-4180.
     */
    public static String escapeCsv(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * Checks if a CSV record string has unclosed quotes.
     */
    private static boolean hasUnbalancedQuotes(String str) {
        int quoteCount = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '"') {
                quoteCount++;
            }
        }
        return (quoteCount % 2) != 0;
    }

    /**
     * Parses a CSV row adhering to RFC-4180 rules.
     */
    public static List<String> parseCsvFields(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Escaped quote ("")
                    current.append('"');
                    i++;
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }
}
