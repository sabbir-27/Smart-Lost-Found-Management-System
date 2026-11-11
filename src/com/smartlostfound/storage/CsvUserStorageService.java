package com.smartlostfound.storage;

import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * RFC-4180 compliant CSV persistence service for user accounts.
 * Safely serializes credentials, contact details, and roles to disk.
 */
public class CsvUserStorageService implements UserStorageService {
    private static final String CSV_HEADER =
            "USERNAME,PASSWORD_HASH,ROLE,NAME,EMAIL,PHONE,CREATED_AT,ACTIVE";

    private final Path filePath;

    public CsvUserStorageService(Path filePath) {
        this.filePath = filePath;
    }

    public CsvUserStorageService(String pathString) {
        this(Paths.get(pathString));
    }

    @Override
    public synchronized List<UserAccount> loadUsers() throws DataPersistenceException {
        List<UserAccount> users = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return users;
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
                    continue;
                }

                String completeRecord = multiLineBuffer.toString().trim();
                if (completeRecord.isEmpty()) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    if (completeRecord.toUpperCase().startsWith("USERNAME,")) {
                        continue;
                    }
                }

                try {
                    UserAccount user = parseCsvRecord(completeRecord);
                    if (user != null) {
                        users.add(user);
                    }
                } catch (Exception ex) {
                    System.err.println("Warning: Skipping corrupted user CSV record: " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to read user CSV storage file: " + filePath, e);
        }

        return users;
    }

    @Override
    public synchronized void saveUsers(List<UserAccount> users) throws DataPersistenceException {
        try {
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Path tempFile = Files.createTempFile(parentDir != null ? parentDir : Paths.get("."), "users_", ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                for (UserAccount user : users) {
                    writer.write(serializeToCsv(user));
                    writer.newLine();
                }
            }

            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new DataPersistenceException("Failed to save user accounts to CSV storage: " + filePath, e);
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

    private UserAccount parseCsvRecord(String line) {
        List<String> tokens = parseCsvLine(line);
        if (tokens.size() < 8) {
            return null;
        }

        String username = tokens.get(0).trim().toLowerCase();
        String passwordHash = tokens.get(1).trim();
        String roleStr = tokens.get(2).trim();
        String name = tokens.get(3).trim();
        String email = tokens.get(4).trim();
        String phone = tokens.get(5).trim();
        String createdAtStr = tokens.get(6).trim();
        String activeStr = tokens.get(7).trim();

        UserRole role = UserRole.USER;
        try {
            role = UserRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }

        LocalDateTime createdAt = LocalDateTime.now();
        if (!createdAtStr.isEmpty()) {
            try {
                createdAt = LocalDateTime.parse(createdAtStr);
            } catch (DateTimeParseException ignored) {
            }
        }

        boolean active = activeStr.isEmpty() || Boolean.parseBoolean(activeStr);

        return new UserAccount(username, passwordHash, role, name, email, phone, createdAt, active);
    }

    private String serializeToCsv(UserAccount user) {
        return String.join(",",
                escapeCsv(user.getUsername()),
                escapeCsv(user.getPasswordHash()),
                escapeCsv(user.getRole().name()),
                escapeCsv(user.getName()),
                escapeCsv(user.getEmail()),
                escapeCsv(user.getPhone()),
                escapeCsv(user.getCreatedAt().toString()),
                escapeCsv(String.valueOf(user.isActive()))
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    private boolean hasUnbalancedQuotes(String str) {
        int quoteCount = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '"') {
                quoteCount++;
            }
        }
        return (quoteCount % 2) != 0;
    }
}
