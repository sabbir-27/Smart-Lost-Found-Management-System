package com.smartlostfound;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.*;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.storage.CsvItemStorageService;
import com.smartlostfound.view.ItemTableModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Automated test suite validating Domain Models, Business Services,
 * CSV Persistence, Polymorphism, Encapsulation, and Controller Operations.
 */
public class TestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("Running Smart Lost & Found Automated Test Suite");
        System.out.println("==========================================================");

        testUserModel();
        testItemPolymorphismAndEncapsulation();
        testManagerOperationsAndIdGeneration();
        testSearchAndFilterLogic();
        testStatusUpdatesAndStatistics();
        testCsvPersistenceAndEdgeCases();
        testTableModelIntegration();
        testControllerOperations();

        System.out.println("==========================================================");
        System.out.printf("Test Results: %d Passed, %d Failed\n", testsPassed, testsFailed);
        System.out.println("==========================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String testName) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: <" + expected + ">, Got: <" + actual + ">");
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> Expected: true, Got: false");
            testsFailed++;
        }
    }

    private static void testUserModel() {
        System.out.println("\n--- 1. Testing User Model ---");
        User user = new User("Alice Smith", "alice@example.com", "555-1234");
        assertEquals("Alice Smith", user.getName(), "User name matches");
        assertEquals("alice@example.com", user.getEmail(), "User email matches");
        assertEquals("555-1234", user.getPhone(), "User phone matches");
        assertTrue(user.isValid(), "User with name and contacts is valid");

        User emptyUser = new User();
        assertTrue(!emptyUser.isValid(), "Empty user is invalid");

        User nameOnly = new User("Bob");
        assertTrue(!nameOnly.isValid(), "User with name only is invalid without contact");

        assertEquals("alice@example.com | 555-1234", user.getContactDetails(), "Contact details format");
    }

    private static void testItemPolymorphismAndEncapsulation() {
        System.out.println("\n--- 2. Testing Item Polymorphism & Encapsulation ---");
        User reporter = new User("John Doe", "john@example.com", "555-9876");

        Item lost = new LostItem("LOST-9001", "Silver Ring", ItemCategory.JEWELRY_WATCH,
                LocalDate.now(), "Engraved with initials", ItemStatus.LOST, reporter,
                "Park Bench", 75.0);

        Item found = new FoundItem("FND-9001", "Car Keys", ItemCategory.KEYS,
                LocalDate.now(), "Honda key fob with blue lanyard", ItemStatus.FOUND, reporter,
                "Cafeteria Floor", "Front Desk Drawer 1", "Found near table 5");

        // Polymorphic methods
        assertEquals("Lost", lost.getItemType(), "LostItem returns 'Lost'");
        assertEquals("Found", found.getItemType(), "FoundItem returns 'Found'");
        assertEquals("Park Bench", lost.getSpecificLocation(), "LostItem specific location");
        assertEquals("Cafeteria Floor", found.getSpecificLocation(), "FoundItem specific location");
        assertEquals("Reward: $75.00", lost.getSpecificDetailsSummary(), "LostItem specific summary");
        assertEquals("Stored: Front Desk Drawer 1", found.getSpecificDetailsSummary(), "FoundItem specific summary");

        // Encapsulation validation
        try {
            lost.setName("");
            assertTrue(false, "Setting empty name should throw exception");
        } catch (IllegalArgumentException e) {
            assertTrue(true, "Encapsulation prevents empty name on Item");
        }

        try {
            ((LostItem) lost).setRewardAmount(-10.0);
            assertTrue(false, "Setting negative reward should throw exception");
        } catch (IllegalArgumentException e) {
            assertTrue(true, "Encapsulation prevents negative reward on LostItem");
        }
    }

    private static void testManagerOperationsAndIdGeneration() {
        System.out.println("\n--- 3. Testing Manager Operations & ID Generation ---");
        try {
            Path tempCsv = Files.createTempFile("test_mgr_", ".csv");
            CsvItemStorageService storage = new CsvItemStorageService(tempCsv);
            LostFoundManager manager = new LostFoundManager(storage);

            // Sample data loaded automatically
            assertTrue(manager.getAllItems().size() > 0, "Initial sample items loaded");

            User reporter = new User("Clara Oswald", "clara@tardis.org", "555-2233");
            LostItem lost = manager.reportLostItem(
                    "Antique Pocket Watch", ItemCategory.JEWELRY_WATCH, LocalDate.now(),
                    "Gold mechanical pocket watch", reporter, "Clock Tower", 200.0);

            assertTrue(lost.getId().startsWith("LOST-"), "Generated ID starts with LOST-");
            Optional<Item> fetched = manager.getItemById(lost.getId());
            assertTrue(fetched.isPresent(), "Newly registered item is retrievable by ID");
            assertEquals("Antique Pocket Watch", fetched.get().getName(), "Item name retrieved matches");

            FoundItem found = manager.reportFoundItem(
                    "Leather Notebook", ItemCategory.BOOKS_STATIONERY, LocalDate.now(),
                    "Black moleskine notebook with sketches", reporter, "Library Hall",
                    "Security Safe 2", "Handed in by student");

            assertTrue(found.getId().startsWith("FND-"), "Generated ID starts with FND-");
            assertEquals("Leather Notebook", manager.getItemById(found.getId()).get().getName(), "Found item retrievable");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testSearchAndFilterLogic() {
        System.out.println("\n--- 4. Testing Search & Filter Logic ---");
        try {
            Path tempCsv = Files.createTempFile("test_search_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));

            // Search by keyword "MacBook"
            List<Item> macbookResults = manager.searchAndFilter("MacBook", null, null, "ALL");
            assertTrue(!macbookResults.isEmpty(), "Search finds 'MacBook'");
            assertEquals("LOST-1001", macbookResults.get(0).getId(), "First result is LOST-1001");

            // Filter by Type "LOST"
            List<Item> lostOnly = manager.searchAndFilter("", null, null, "LOST");
            for (Item item : lostOnly) {
                assertTrue(item instanceof LostItem, "Every item in LOST filter is a LostItem");
            }

            // Filter by Type "FOUND"
            List<Item> foundOnly = manager.searchAndFilter("", null, null, "FOUND");
            for (Item item : foundOnly) {
                assertTrue(item instanceof FoundItem, "Every item in FOUND filter is a FoundItem");
            }

            // Filter by Category
            List<Item> keyItems = manager.searchAndFilter("", ItemCategory.KEYS, null, "ALL");
            for (Item item : keyItems) {
                assertEquals(ItemCategory.KEYS, item.getCategory(), "Every item has KEYS category");
            }

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testStatusUpdatesAndStatistics() {
        System.out.println("\n--- 5. Testing Status Updates & Statistics ---");
        try {
            Path tempCsv = Files.createTempFile("test_stats_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));

            // Update status of LOST-1001 to RECOVERED
            Item updated = manager.updateItemStatus("LOST-1001", ItemStatus.RECOVERED, "Returned to owner Alex.");
            assertEquals(ItemStatus.RECOVERED, updated.getStatus(), "Status changed to RECOVERED");
            assertEquals("Returned to owner Alex.", updated.getResolutionNotes(), "Resolution notes saved");
            assertTrue(updated.isResolved(), "Item is now resolved");

            LostFoundManager.SystemStats stats = manager.getStatistics();
            assertTrue(stats.getRecoveredCount() >= 1, "Recovered count reflects update");
            assertEquals(stats.getTotalCount(), stats.getLostCount() + stats.getFoundCount(), "Total equals lost + found");

            // Delete item
            boolean deleted = manager.deleteItem("LOST-1001");
            assertTrue(deleted, "Item was deleted");
            assertTrue(!manager.getItemById("LOST-1001").isPresent(), "Deleted item is no longer found");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testCsvPersistenceAndEdgeCases() {
        System.out.println("\n--- 6. Testing CSV Persistence & RFC-4180 Edge Cases ---");
        try {
            Path tempCsv = Files.createTempFile("test_persist_", ".csv");
            CsvItemStorageService storage = new CsvItemStorageService(tempCsv);

            User reporter = new User("Complex Name, with Comma", "quote\"email@test.com", "123-456");
            Item lost = new LostItem("LOST-9999", "Special \"Quoted\" Item, with Comma",
                    ItemCategory.ELECTRONICS, LocalDate.now(),
                    "Line 1 description\nLine 2 with comma, and \"quotes\"",
                    ItemStatus.LOST, reporter, "Building A, Floor 2", 99.50);

            Item found = new FoundItem("FND-9999", "Gym Bag",
                    ItemCategory.BAG_BACKPACK, LocalDate.now(),
                    "Red bag with sneakers",
                    ItemStatus.FOUND, reporter, "Gymnasium", "Room 101, Cabinet B", "None");

            storage.saveItems(List.of(lost, found));

            List<Item> reloaded = storage.loadItems();
            assertEquals(2, reloaded.size(), "Reloaded 2 items");

            Item reloadedLost = reloaded.get(0);
            assertTrue(reloadedLost instanceof LostItem, "Reloaded item 0 is polymorphic LostItem");
            assertEquals("Special \"Quoted\" Item, with Comma", reloadedLost.getName(), "Name preserved with quotes and commas");
            assertEquals(99.50, ((LostItem) reloadedLost).getRewardAmount(), "Reward amount preserved");
            assertEquals("Complex Name, with Comma", reloadedLost.getReporter().getName(), "Reporter name with comma preserved");

            Item reloadedFound = reloaded.get(1);
            assertTrue(reloadedFound instanceof FoundItem, "Reloaded item 1 is polymorphic FoundItem");
            assertEquals("Room 101, Cabinet B", ((FoundItem) reloadedFound).getStorageLocation(), "Storage location preserved");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testTableModelIntegration() {
        System.out.println("\n--- 7. Testing Table Model Integration ---");
        User user = new User("Sam", "sam@example.com", "555");
        Item item = new LostItem("LOST-1111", "iPhone 15", ItemCategory.ELECTRONICS,
                LocalDate.of(2026, 11, 10), "Black case", ItemStatus.LOST, user, "Room 303", 50.0);

        ItemTableModel model = new ItemTableModel(List.of(item));
        assertEquals(1, model.getRowCount(), "Table row count");
        assertEquals(9, model.getColumnCount(), "Table column count");
        assertEquals("LOST-1111", model.getValueAt(0, 0), "Column 0 is ID");
        assertEquals("Lost", model.getValueAt(0, 1), "Column 1 is Type");
        assertEquals("iPhone 15", model.getValueAt(0, 2), "Column 2 is Name");
        assertEquals(ItemStatus.LOST, model.getValueAt(0, 7), "Column 7 is ItemStatus object");
        assertEquals(item, model.getItemAt(0), "getItemAt returns matching Item object");
    }

    private static void testControllerOperations() {
        System.out.println("\n--- 8. Testing Controller Operations ---");
        try {
            Path tempCsv = Files.createTempFile("test_ctrl_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));
            LostFoundController controller = new LostFoundController(manager);

            assertTrue(controller.getStoragePathDescription().contains(".csv"), "Storage path description valid");
            Files.deleteIfExists(tempCsv);
            System.out.println("  [PASS] Controller initialized and queryable");
            testsPassed++;
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }
}
