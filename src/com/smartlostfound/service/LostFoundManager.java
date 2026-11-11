package com.smartlostfound.service;

import com.smartlostfound.model.*;
import com.smartlostfound.storage.CsvItemStorageService;
import com.smartlostfound.storage.DataPersistenceException;
import com.smartlostfound.storage.ItemStorageService;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Core business logic and service manager for the Smart Lost &amp; Found system.
 * Manages item collections, automated ID sequences, search and filtering pipelines,
 * lifecycle status transitions, analytical statistics, and persistence synchronization.
 */
public class LostFoundManager {
    private static final String DEFAULT_STORAGE_PATH = "data/lost_found_items.csv";

    private final List<Item> items = new CopyOnWriteArrayList<>();
    private final ItemStorageService storageService;
    private final AtomicInteger lostIdSequence = new AtomicInteger(1000);
    private final AtomicInteger foundIdSequence = new AtomicInteger(2000);

    /**
     * Default constructor using default CSV storage location ("data/lost_found_items.csv").
     */
    public LostFoundManager() {
        this(new CsvItemStorageService(Paths.get(DEFAULT_STORAGE_PATH)));
    }

    /**
     * Constructs a manager with a specific storage service implementation.
     *
     * @param storageService Implementation of {@link ItemStorageService}
     */
    public LostFoundManager(ItemStorageService storageService) {
        this.storageService = Objects.requireNonNull(storageService, "Storage service cannot be null");
        initializeData();
    }

    /**
     * Initializes data from storage; if no storage exists, populates realistic sample data.
     */
    public synchronized void initializeData() {
        try {
            if (storageService.storageExists()) {
                List<Item> loaded = storageService.loadItems();
                items.clear();
                if (!loaded.isEmpty()) {
                    items.addAll(loaded);
                    synchronizeIdSequences();
                } else {
                    loadSampleData();
                    persistCurrentState();
                }
            } else {
                loadSampleData();
                persistCurrentState();
            }
        } catch (DataPersistenceException e) {
            System.err.println("Failed to load items from storage, falling back to sample data: " + e.getMessage());
            loadSampleData();
        }
    }

    /**
     * Persists all current in-memory items to the configured storage service.
     *
     * @throws DataPersistenceException if writing to disk fails
     */
    public synchronized void persistCurrentState() throws DataPersistenceException {
        storageService.saveItems(new ArrayList<>(items));
    }

    /**
     * Registers a new LostItem with automatic ID generation and persistence.
     *
     * @param name Name of the item
     * @param category Classification category
     * @param date Date when lost
     * @param description Narrative description
     * @param reporter Person reporting the loss
     * @param locationLost Specific location where lost
     * @param rewardAmount Optional reward offered (>= 0.0)
     * @return The registered {@link LostItem} instance
     * @throws DataPersistenceException if persistence fails
     */
    public synchronized LostItem reportLostItem(String name, ItemCategory category, LocalDate date,
                                                String description, User reporter,
                                                String locationLost, double rewardAmount)
            throws DataPersistenceException {
        String id = generateNextLostId();
        LostItem item = new LostItem(id, name, category, date, description, ItemStatus.LOST,
                reporter, locationLost, rewardAmount);
        items.add(0, item); // Insert at front for newest-first display
        persistCurrentState();
        return item;
    }

    /**
     * Registers a new FoundItem with automatic ID generation and persistence.
     *
     * @param name Name of the item
     * @param category Classification category
     * @param date Date discovered
     * @param description Narrative description
     * @param reporter Person reporting the found item
     * @param locationFound Specific place where found
     * @param storageLocation Holding location where stored
     * @param finderContactInfo Additional contact information
     * @return The registered {@link FoundItem} instance
     * @throws DataPersistenceException if persistence fails
     */
    public synchronized FoundItem reportFoundItem(String name, ItemCategory category, LocalDate date,
                                                  String description, User reporter,
                                                  String locationFound, String storageLocation,
                                                  String finderContactInfo)
            throws DataPersistenceException {
        String id = generateNextFoundId();
        FoundItem item = new FoundItem(id, name, category, date, description, ItemStatus.FOUND,
                reporter, locationFound, storageLocation, finderContactInfo);
        items.add(0, item);
        persistCurrentState();
        return item;
    }

    /**
     * Updates an existing item's status and adds optional resolution notes.
     *
     * @param itemId Unique ID of item to update
     * @param newStatus New status to set
     * @param resolutionNotes Optional notes explaining resolution/claimer info
     * @return The updated {@link Item}
     * @throws NoSuchElementException if item ID is not found
     * @throws DataPersistenceException if persistence fails
     */
    public synchronized Item updateItemStatus(String itemId, ItemStatus newStatus, String resolutionNotes)
            throws DataPersistenceException {
        Item item = getItemById(itemId)
                .orElseThrow(() -> new NoSuchElementException("No item found with ID: " + itemId));

        item.setStatus(newStatus);
        if (resolutionNotes != null && !resolutionNotes.trim().isEmpty()) {
            item.setResolutionNotes(resolutionNotes.trim());
        }
        persistCurrentState();
        return item;
    }

    /**
     * Deletes an item by its ID.
     *
     * @param itemId Unique identifier of item
     * @return True if item was found and removed
     * @throws DataPersistenceException if persistence fails
     */
    public synchronized boolean deleteItem(String itemId) throws DataPersistenceException {
        boolean removed = items.removeIf(item -> item.getId().equalsIgnoreCase(itemId));
        if (removed) {
            persistCurrentState();
        }
        return removed;
    }

    /**
     * Finds an item by its unique ID.
     *
     * @param itemId Unique ID string
     * @return Optional containing the item if found
     */
    public Optional<Item> getItemById(String itemId) {
        if (itemId == null) return Optional.empty();
        return items.stream()
                .filter(item -> item.getId().equalsIgnoreCase(itemId.trim()))
                .findFirst();
    }

    /**
     * Returns an unmodifiable snapshot of all items.
     *
     * @return List of all items
     */
    public List<Item> getAllItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * Flexible multi-criteria search and filter pipeline.
     *
     * @param keyword Search term matching ID, name, description, location, or reporter
     * @param category Specific category to filter by (or null for all)
     * @param status Specific status to filter by (or null for all)
     * @param typeFilter Item type filter ("ALL", "LOST", "FOUND")
     * @return Filtered and sorted list of items (most recent first)
     */
    public List<Item> searchAndFilter(String keyword, ItemCategory category, ItemStatus status, String typeFilter) {
        return items.stream()
                .filter(item -> {
                    // Type filter
                    if (typeFilter != null && !typeFilter.equalsIgnoreCase("ALL")) {
                        if (typeFilter.equalsIgnoreCase("LOST") && !(item instanceof LostItem)) {
                            return false;
                        }
                        if (typeFilter.equalsIgnoreCase("FOUND") && !(item instanceof FoundItem)) {
                            return false;
                        }
                    }

                    // Category filter
                    if (category != null && item.getCategory() != category) {
                        return false;
                    }

                    // Status filter
                    if (status != null && item.getStatus() != status) {
                        return false;
                    }

                    // Keyword match
                    return item.matchesKeyword(keyword);
                })
                .sorted(Comparator.comparing(Item::getDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Item::getId, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates operational dashboard metrics and statistics.
     *
     * @return Aggregated {@link SystemStats}
     */
    public SystemStats getStatistics() {
        int total = items.size();
        int lost = 0;
        int found = 0;
        int recovered = 0;
        int claimed = 0;
        int archived = 0;
        double totalReward = 0.0;

        for (Item item : items) {
            if (item instanceof LostItem) {
                lost++;
                totalReward += ((LostItem) item).getRewardAmount();
            } else if (item instanceof FoundItem) {
                found++;
            }

            if (item.getStatus() == ItemStatus.RECOVERED) {
                recovered++;
            } else if (item.getStatus() == ItemStatus.CLAIMED) {
                claimed++;
            } else if (item.getStatus() == ItemStatus.ARCHIVED) {
                archived++;
            }
        }

        int resolved = recovered + claimed + archived;
        int active = total - resolved;

        return new SystemStats(total, lost, found, recovered, claimed, active, resolved, totalReward);
    }

    /**
     * Storage destination path accessor.
     *
     * @return Path string of storage
     */
    public String getStoragePath() {
        return storageService.getStoragePath();
    }

    // =========================================================================
    // ID Generator & Synchronization
    // =========================================================================

    private String generateNextLostId() {
        return String.format("LOST-%04d", lostIdSequence.incrementAndGet());
    }

    private String generateNextFoundId() {
        return String.format("FND-%04d", foundIdSequence.incrementAndGet());
    }

    private void synchronizeIdSequences() {
        int maxLost = 1000;
        int maxFound = 2000;

        for (Item item : items) {
            String id = item.getId();
            try {
                if (id.startsWith("LOST-")) {
                    int num = Integer.parseInt(id.substring(5));
                    if (num > maxLost) maxLost = num;
                } else if (id.startsWith("FND-")) {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > maxFound) maxFound = num;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        lostIdSequence.set(maxLost);
        foundIdSequence.set(maxFound);
    }

    /**
     * Loads realistic, diverse sample data to provide immediate value on first run.
     */
    private void loadSampleData() {
        items.clear();

        // Sample 1: Lost Laptop
        items.add(new LostItem(
                "LOST-1001",
                "MacBook Pro 14\" M2",
                ItemCategory.ELECTRONICS,
                LocalDate.now().minusDays(2),
                "Space Gray MacBook with carbon fiber skin. Serial ending in 89Q2.",
                ItemStatus.LOST,
                new User("Alex Chen", "alex.chen@university.edu", "+1 (555) 234-5678"),
                "Science Library, 3rd Floor Quiet Study Desk #14",
                100.00
        ));

        // Sample 2: Found Backpack
        items.add(new FoundItem(
                "FND-2001",
                "Navy Osprey Backpack",
                ItemCategory.BAG_BACKPACK,
                LocalDate.now().minusDays(1),
                "Contains spiral notebooks, graphing calculator, and blue insulated water bottle.",
                ItemStatus.FOUND,
                new User("Campus Security Officer Davis", "security@university.edu", "+1 (555) 999-0100"),
                "Campus Center Cafeteria East Booth",
                "Central Security Office, Locker #08",
                "Turned in by cafeteria staff"
        ));

        // Sample 3: Lost Wallet
        items.add(new LostItem(
                "LOST-1002",
                "Brown Leather Bi-Fold Wallet",
                ItemCategory.WALLET_PURSE,
                LocalDate.now().minusDays(3),
                "Tommy Hilfiger wallet containing Driver's License and Student ID card.",
                ItemStatus.RECOVERED,
                new User("Marcus Vance", "m.vance@gmail.com", "+1 (555) 872-1144"),
                "Gymnasium Locker Room",
                25.00
        ));
        items.get(2).setResolutionNotes("Returned to Marcus Vance upon student ID verification on " + LocalDate.now());

        // Sample 4: Found Car Keys
        items.add(new FoundItem(
                "FND-2002",
                "Toyota Key Fob with Carabiner",
                ItemCategory.KEYS,
                LocalDate.now().minusDays(4),
                "Single Toyota smart fob with red aluminum carabiner and gym barcode tag.",
                ItemStatus.CLAIMED,
                new User("Rachel Green", "rgreen@citycafe.org", "+1 (555) 432-8877"),
                "Parking Lot B, near Section 3 Lamp Post",
                "Parking Admin Booth A",
                "Owner claimed with duplicate car registration"
        ));
        items.get(3).setResolutionNotes("Claimed by owner matching VIN and barcode tag.");

        // Sample 5: Lost Diamond Earring
        items.add(new LostItem(
                "LOST-1003",
                "White Gold Solitaire Diamond Stud",
                ItemCategory.JEWELRY_WATCH,
                LocalDate.now().minusDays(5),
                "Single stud earring with screw backing. Sentimental graduation gift.",
                ItemStatus.LOST,
                new User("Sophia Martinez", "smartinez@outlook.com", "+1 (555) 671-9922"),
                "Auditorium Hall B during Guest Lecture",
                150.00
        ));

        // Sample 6: Found Passport
        items.add(new FoundItem(
                "FND-2003",
                "International Passport in Leather Case",
                ItemCategory.DOCUMENTS_ID,
                LocalDate.now(),
                "Blue passport cover with initials E.K. turned in to reception.",
                ItemStatus.FOUND,
                new User("Front Desk Receptionist Anna", "anna.desk@campus.edu", "+1 (555) 012-3456"),
                "Administration Building Lobby Bench",
                "Vault Drawer 2, Administration Office",
                "Kept under lock and key awaiting owner verification"
        ));

        synchronizeIdSequences();
    }

    // =========================================================================
    // System Statistics Record / Value Object
    // =========================================================================

    /**
     * Immutable value object holding analytical metrics for UI dashboard display.
     */
    public static class SystemStats {
        private final int totalCount;
        private final int lostCount;
        private final int foundCount;
        private final int recoveredCount;
        private final int claimedCount;
        private final int activeCount;
        private final int resolvedCount;
        private final double totalRewardOffered;

        public SystemStats(int totalCount, int lostCount, int foundCount, int recoveredCount,
                           int claimedCount, int activeCount, int resolvedCount, double totalRewardOffered) {
            this.totalCount = totalCount;
            this.lostCount = lostCount;
            this.foundCount = foundCount;
            this.recoveredCount = recoveredCount;
            this.claimedCount = claimedCount;
            this.activeCount = activeCount;
            this.resolvedCount = resolvedCount;
            this.totalRewardOffered = totalRewardOffered;
        }

        public int getTotalCount() { return totalCount; }
        public int getLostCount() { return lostCount; }
        public int getFoundCount() { return foundCount; }
        public int getRecoveredCount() { return recoveredCount; }
        public int getClaimedCount() { return claimedCount; }
        public int getActiveCount() { return activeCount; }
        public int getResolvedCount() { return resolvedCount; }
        public double getTotalRewardOffered() { return totalRewardOffered; }
    }
}
