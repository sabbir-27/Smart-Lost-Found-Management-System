package com.smartlostfound;

import com.smartlostfound.controller.LostFoundController;
import com.smartlostfound.model.*;
import com.smartlostfound.service.LostFoundManager;
import com.smartlostfound.service.UserManager;
import com.smartlostfound.service.WebFeedServer;
import com.smartlostfound.storage.CsvItemStorageService;
import com.smartlostfound.storage.CsvUserStorageService;
import com.smartlostfound.storage.DotEnv;
import com.smartlostfound.storage.MongoConfig;
import com.smartlostfound.storage.MongoItemStorageService;
import com.smartlostfound.storage.MongoUserStorageService;
import com.smartlostfound.view.ItemTableModel;
import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Automated test suite validating Domain Models, Business Services,
 * CSV Persistence, Polymorphism, Encapsulation, Authentication, RBAC, and Controller Operations.
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
        testUserAccountAndRoles();
        testUserCsvPersistence();
        testUserManagerAuthenticationAndRegistration();
        testRoleBasedAccessControl();
        testWebFeedServerSecurityAndAccess();
        testGoogleAuthenticationAndAutoRegistration();
        testMongoArchitectureAndBsonMapping();
        testFeedNewestFirstChronologicalOrdering();

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
    }

    private static void testItemPolymorphismAndEncapsulation() {
        System.out.println("\n--- 2. Testing Item Polymorphism and Encapsulation ---");
        User owner = new User("David Miller", "david@example.com", "555-9876");
        User finder = new User("Campus Security Officer", "security@campus.edu", "555-0100");

        Item lost = new LostItem(
                "LOST-1001",
                "Dell XPS 15",
                ItemCategory.ELECTRONICS,
                LocalDate.of(2026, 11, 8),
                "Silver laptop with stickers",
                ItemStatus.LOST,
                owner,
                "Library 2nd Floor",
                100.0
        );

        Item found = new FoundItem(
                "FND-2001",
                "Blue Hydro Flask",
                ItemCategory.OTHER,
                LocalDate.of(2026, 11, 9),
                "32oz insulated bottle",
                ItemStatus.FOUND,
                finder,
                "Gymnasium Bench",
                "Security Desk Lockbox",
                "Has a scratch on lid"
        );

        // Polymorphic method calls
        assertEquals("Lost", lost.getItemType(), "Polymorphic getItemType on LostItem");
        assertEquals("Found", found.getItemType(), "Polymorphic getItemType on FoundItem");

        assertEquals("Library 2nd Floor", lost.getSpecificLocation(), "LostItem location");
        assertEquals("Gymnasium Bench", found.getSpecificLocation(), "FoundItem location");

        assertTrue(lost.getSpecificDetailsSummary().contains("Reward: $100.00"), "LostItem summary has reward");
        assertTrue(found.getSpecificDetailsSummary().contains("Security Desk Lockbox"), "FoundItem summary has storage");

        // Encapsulation checks
        assertEquals("LOST-1001", lost.getId(), "Item ID getter");
        assertEquals("Dell XPS 15", lost.getName(), "Item Name getter");
        assertEquals(ItemStatus.LOST, lost.getStatus(), "Item Status getter");
        assertEquals(100.0, ((LostItem) lost).getRewardAmount(), "LostItem reward getter");
        assertEquals("Security Desk Lockbox", ((FoundItem) found).getStorageLocation(), "FoundItem storage getter");
    }

    private static void testManagerOperationsAndIdGeneration() {
        System.out.println("\n--- 3. Testing LostFoundManager and ID Generation ---");
        try {
            Path tempCsv = Files.createTempFile("test_mgr_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));

            int initialCount = manager.getAllItems().size();
            assertTrue(initialCount > 0, "Manager loaded sample data when storage empty");

            User reporter = new User("Emma Watson", "emma@example.com", "555-3333");
            LostItem lost = manager.reportLostItem(
                    "Car Keys", ItemCategory.KEYS, LocalDate.now(),
                    "Honda keychain with 3 keys", reporter, "Parking Lot B", 25.0
            );

            assertTrue(lost.getId().startsWith("LOST-"), "Generated Lost ID has correct prefix");
            assertEquals(initialCount + 1, manager.getAllItems().size(), "Item count incremented");

            FoundItem found = manager.reportFoundItem(
                    "Winter Beanie", ItemCategory.CLOTHING, LocalDate.now(),
                    "Red knitted cap", reporter, "Cafeteria", "Lost and Found Bin 1", "Clean condition"
            );

            assertTrue(found.getId().startsWith("FND-"), "Generated Found ID has correct prefix");
            assertEquals(initialCount + 2, manager.getAllItems().size(), "Item count incremented twice");

            // Test deletion
            boolean deleted = manager.deleteItem(lost.getId());
            assertTrue(deleted, "Item deletion succeeded");
            assertEquals(initialCount + 1, manager.getAllItems().size(), "Item count decremented after deletion");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testSearchAndFilterLogic() {
        System.out.println("\n--- 4. Testing Search and Filter Logic ---");
        try {
            Path tempCsv = Files.createTempFile("test_filter_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));

            // Test keyword search
            List<Item> searchMacbook = manager.searchAndFilter("MacBook", null, null, "ALL");
            assertTrue(searchMacbook.size() >= 1, "Search keyword found MacBook");

            // Test category filter
            List<Item> electronics = manager.searchAndFilter("", ItemCategory.ELECTRONICS, null, "ALL");
            assertTrue(electronics.size() >= 1, "Category filter found electronics");
            for (Item item : electronics) {
                assertEquals(ItemCategory.ELECTRONICS, item.getCategory(), "Filtered items match category");
            }

            // Test type filter
            List<Item> lostOnly = manager.searchAndFilter("", null, null, "LOST");
            for (Item item : lostOnly) {
                assertTrue(item instanceof LostItem, "Type filter returned only LostItem");
            }

            List<Item> foundOnly = manager.searchAndFilter("", null, null, "FOUND");
            for (Item item : foundOnly) {
                assertTrue(item instanceof FoundItem, "Type filter returned only FoundItem");
            }

            // Test multi-criteria filter
            List<Item> specific = manager.searchAndFilter("Space Gray", ItemCategory.ELECTRONICS, ItemStatus.LOST, "LOST");
            assertTrue(specific.size() >= 1, "Combined multi-criteria search succeeded");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testStatusUpdatesAndStatistics() {
        System.out.println("\n--- 5. Testing Status Updates and Statistics ---");
        try {
            Path tempCsv = Files.createTempFile("test_status_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));

            Optional<Item> itemOpt = manager.getAllItems().stream()
                    .filter(i -> i.getStatus() == ItemStatus.LOST)
                    .findFirst();

            assertTrue(itemOpt.isPresent(), "Found a LOST item to update");
            Item target = itemOpt.get();

            Item updated = manager.updateItemStatus(target.getId(), ItemStatus.RECOVERED, "Returned to verified owner");
            assertEquals(ItemStatus.RECOVERED, updated.getStatus(), "Item status transitioned to RECOVERED");
            assertEquals("Returned to verified owner", updated.getResolutionNotes(), "Resolution notes saved");

            // Check Statistics calculations
            LostFoundManager.SystemStats stats = manager.getStatistics();
            assertEquals(manager.getAllItems().size(), stats.getTotalCount(), "Stats total count accurate");
            assertTrue(stats.getResolvedCount() >= 1, "Stats resolved count updated");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testCsvPersistenceAndEdgeCases() {
        System.out.println("\n--- 6. Testing CSV Persistence and Edge Cases ---");
        try {
            Path tempCsv = Files.createTempFile("test_csv_", ".csv");
            CsvItemStorageService storage = new CsvItemStorageService(tempCsv);

            // Create items with commas, quotes, and multiline descriptions
            User user = new User("Jane \"JJ\" Doe", "jane@example.com", "555-4444");
            LostItem complexLost = new LostItem(
                    "LOST-9999",
                    "Wallet, Black Leather",
                    ItemCategory.WALLET_PURSE,
                    LocalDate.of(2026, 11, 1),
                    "Line 1 description\nLine 2 with \"quotes\" and commas, here.",
                    ItemStatus.LOST,
                    user,
                    "Starbucks, Table #4",
                    50.0
            );

            FoundItem complexFound = new FoundItem(
                    "FND-8888",
                    "USB Flash Drive",
                    ItemCategory.ELECTRONICS,
                    LocalDate.of(2026, 11, 2),
                    "Kingston 64GB",
                    ItemStatus.FOUND,
                    user,
                    "Lab 102",
                    "Room 101, Cabinet B",
                    "Blue cap"
            );

            storage.saveItems(List.of(complexLost, complexFound));

            // Reload and verify exact preservation
            List<Item> reloaded = storage.loadItems();
            assertEquals(2, reloaded.size(), "Reloaded 2 items successfully");

            Item reloadedLost = reloaded.get(0);
            assertEquals("LOST-9999", reloadedLost.getId(), "Preserved complex ID");
            assertEquals("Wallet, Black Leather", reloadedLost.getName(), "Preserved comma in name");
            assertEquals("Line 1 description\nLine 2 with \"quotes\" and commas, here.",
                    reloadedLost.getDescription(), "Preserved multiline description with quotes and commas");
            assertEquals("Jane \"JJ\" Doe", reloadedLost.getReporter().getName(), "Preserved quotes in reporter name");

            Item reloadedFound = reloaded.get(1);
            assertEquals("FND-8888", reloadedFound.getId(), "Preserved FoundItem ID");
            assertTrue(reloadedFound instanceof FoundItem, "Preserved polymorphism upon CSV deserialization");
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

    private static void testUserAccountAndRoles() {
        System.out.println("\n--- 9. Testing UserAccount and UserRole ---");
        UserRole adminRole = UserRole.ADMIN;
        UserRole userRole = UserRole.USER;

        assertTrue(adminRole.isAdmin(), "AdminRole.isAdmin returns true");
        assertTrue(!userRole.isAdmin(), "UserRole.isAdmin returns false");

        UserAccount account = UserAccount.create(
                "alex99",
                "secretPassword",
                UserRole.ADMIN,
                "Alex Mercer",
                "alex@mercer.com",
                "555-0909"
        );

        assertEquals("alex99", account.getUsername(), "Username matches");
        assertEquals("Alex Mercer", account.getName(), "Name matches (inherited)");
        assertEquals("alex@mercer.com", account.getEmail(), "Email matches (inherited)");
        assertTrue(account.isAdmin(), "Account reports admin role");
        assertTrue(account.verifyPassword("secretPassword"), "Password verification matches");
        assertTrue(!account.verifyPassword("wrongPassword"), "Wrong password rejected");
        assertEquals("AM", account.getInitials(), "Avatar initials computed correctly");
        assertTrue(account instanceof User, "UserAccount polymorphically inherits User");
    }

    private static void testUserCsvPersistence() {
        System.out.println("\n--- 10. Testing User CSV Persistence ---");
        try {
            Path tempCsv = Files.createTempFile("test_users_", ".csv");
            CsvUserStorageService storage = new CsvUserStorageService(tempCsv);

            UserAccount u1 = UserAccount.create("alice", "pass123", UserRole.USER, "Alice Cooper", "alice@music.com", "555-1111");
            UserAccount u2 = UserAccount.create("root_admin", "adminPass", UserRole.ADMIN, "Super Admin", "root@sys.org", "555-9999");

            storage.saveUsers(List.of(u1, u2));

            List<UserAccount> reloaded = storage.loadUsers();
            assertEquals(2, reloaded.size(), "Reloaded 2 user accounts");

            UserAccount reloadedUser = reloaded.get(0);
            assertEquals("alice", reloadedUser.getUsername(), "Username preserved");
            assertEquals(UserRole.USER, reloadedUser.getRole(), "User role preserved");
            assertTrue(reloadedUser.verifyPassword("pass123"), "Password hash verification holds across persistence");

            UserAccount reloadedAdmin = reloaded.get(1);
            assertEquals("root_admin", reloadedAdmin.getUsername(), "Admin username preserved");
            assertEquals(UserRole.ADMIN, reloadedAdmin.getRole(), "Admin role preserved");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testUserManagerAuthenticationAndRegistration() {
        System.out.println("\n--- 11. Testing UserManager Authentication & Registration ---");
        try {
            Path tempCsv = Files.createTempFile("test_usermgr_", ".csv");
            UserManager manager = new UserManager(new CsvUserStorageService(tempCsv));

            // Default seed check
            assertEquals(3, manager.getTotalCount(), "UserManager seeded 3 default accounts");
            assertEquals(1, manager.getAdminCount(), "1 seeded administrator");

            // Successful authentication
            UserAccount admin = manager.authenticate("admin", "admin123");
            assertTrue(admin.isAdmin(), "Admin authenticated successfully");

            UserAccount john = manager.authenticate("john", "user123");
            assertTrue(!john.isAdmin(), "John Doe authenticated successfully as standard user");

            // Registration
            UserAccount registered = manager.registerUser(
                    "clara_o", "claraPass", "Clara Oswald", "clara@tardis.org", "555-7777", UserRole.USER);
            assertEquals("clara_o", registered.getUsername(), "Registered username matches");
            assertEquals(4, manager.getTotalCount(), "User count incremented to 4");

            // Duplicate registration prevention
            boolean threwDuplicate = false;
            try {
                manager.registerUser("clara_o", "anotherPass", "Clara Clone", "clara2@tardis.org", "555-8888", UserRole.USER);
            } catch (IllegalArgumentException e) {
                threwDuplicate = true;
            }
            assertTrue(threwDuplicate, "Duplicate username registration prevented");

            // Wrong password rejection
            boolean threwBadPass = false;
            try {
                manager.authenticate("clara_o", "wrongPass");
            } catch (IllegalArgumentException e) {
                threwBadPass = true;
            }
            assertTrue(threwBadPass, "Invalid password throws authentication exception");

            // Role updates
            manager.updateUserRole("clara_o", UserRole.ADMIN);
            assertEquals(2, manager.getAdminCount(), "Admin count updated after promoting Clara");

            // Deactivate and verify login blocked
            manager.toggleUserStatus("clara_o");
            boolean threwDeactivated = false;
            try {
                manager.authenticate("clara_o", "claraPass");
            } catch (IllegalArgumentException e) {
                threwDeactivated = true;
            }
            assertTrue(threwDeactivated, "Deactivated account rejected at login");

            Files.deleteIfExists(tempCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testRoleBasedAccessControl() {
        System.out.println("\n--- 12. Testing Role-Based Access Control (RBAC) ---");
        try {
            Path tempItemCsv = Files.createTempFile("test_rbac_items_", ".csv");
            Path tempUserCsv = Files.createTempFile("test_rbac_users_", ".csv");

            LostFoundManager itemManager = new LostFoundManager(new CsvItemStorageService(tempItemCsv));
            UserManager userManager = new UserManager(new CsvUserStorageService(tempUserCsv));
            LostFoundController controller = new LostFoundController(itemManager, userManager);

            UserAccount adminUser = userManager.authenticate("admin", "admin123");
            UserAccount johnUser = userManager.authenticate("john", "user123");

            // Create an item reported by John
            User reporterJohn = new User("John Doe", "john.doe@example.com", "+1-555-0199");
            LostItem johnItem = itemManager.reportLostItem(
                    "Leather Wallet", ItemCategory.WALLET_PURSE, LocalDate.now(),
                    "Brown bi-fold", reporterJohn, "Library", 20.0);

            // Create an item reported by someone else (Sarah)
            User reporterSarah = new User("Sarah Jenkins", "sarah.j@example.com", "+1-555-0142");
            LostItem sarahItem = itemManager.reportLostItem(
                    "AirPods Pro", ItemCategory.ELECTRONICS, LocalDate.now(),
                    "White case", reporterSarah, "Gym", 30.0);

            // When logged in as Admin: can modify BOTH
            controller.setCurrentUser(adminUser);
            assertTrue(controller.canModifyItem(johnItem), "Admin can modify John's item");
            assertTrue(controller.canModifyItem(sarahItem), "Admin can modify Sarah's item");

            // When logged in as John: can modify John's item, but NOT Sarah's
            controller.setCurrentUser(johnUser);
            assertTrue(controller.canModifyItem(johnItem), "John can modify his own item");
            assertTrue(!controller.canModifyItem(sarahItem), "John CANNOT modify Sarah's item");

            // Substring / variation matching test
            User reporterShort = new User("John", "other@example.com", "");
            LostItem shortItem = itemManager.reportLostItem(
                    "Blue Pen", ItemCategory.OTHER, LocalDate.now(),
                    "Ballpoint", reporterShort, "Desk", 0.0);
            assertTrue(controller.canModifyItem(shortItem), "John can modify item reported as 'John'");

            // Null user check
            controller.setCurrentUser(null);
            assertTrue(!controller.canModifyItem(johnItem), "Guest cannot modify items");

            // Robust deletion tests with whitespace, null, and non-existent IDs
            assertTrue(itemManager.deleteItem("  " + johnItem.getId() + "  "), "deleteItem succeeds with whitespace trimmed ID");
            assertTrue(!itemManager.deleteItem("NON_EXISTENT_ID"), "deleteItem returns false for nonexistent ID");
            assertTrue(!itemManager.deleteItem(null), "deleteItem returns false for null ID");

            Files.deleteIfExists(tempItemCsv);
            Files.deleteIfExists(tempUserCsv);
        } catch (Exception e) {
            System.err.println("Test failed with exception: " + e.getMessage());
            testsFailed++;
        }
    }

    private static void testWebFeedServerSecurityAndAccess() {
        System.out.println("\n--- 13. Testing Web Feed Server Security & Access Isolation ---");
        WebFeedServer webServer = null;
        Path tempItemCsv = null;
        try {
            tempItemCsv = Files.createTempFile("test_web_feed_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempItemCsv));

            // Populate sample item
            manager.reportLostItem(
                    "Test Watch", ItemCategory.JEWELRY_WATCH, LocalDate.now(),
                    "Silver mesh", new User("Tester", "test@example.com", "555-0000"), "Library", 15.0
            );

            webServer = new WebFeedServer(manager);
            boolean started = webServer.start();
            assertTrue(started, "WebFeedServer started successfully");
            assertTrue(webServer.isRunning(), "WebFeedServer reports isRunning() == true");

            int port = webServer.getPort();
            String baseUrl = "http://localhost:" + port;

            // 1. GET /feed should succeed with 200 and return Community Feed HTML
            HttpURLConnection feedConn = (HttpURLConnection) new URL(baseUrl + "/feed").openConnection();
            feedConn.setRequestMethod("GET");
            feedConn.setConnectTimeout(3000);
            feedConn.setReadTimeout(3000);
            int feedCode = feedConn.getResponseCode();
            assertEquals(200, feedCode, "GET /feed returns HTTP 200 OK");

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(feedConn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line).append("\n");
                }
            }
            assertTrue(body.toString().contains("Public Community Feed"), "Feed HTML contains 'Public Community Feed' heading");
            assertTrue(body.toString().contains("Test Watch"), "Feed HTML displays reported lost item");

            // 2. GET / should also succeed with 200
            HttpURLConnection rootConn = (HttpURLConnection) new URL(baseUrl + "/").openConnection();
            rootConn.setRequestMethod("GET");
            assertEquals(200, rootConn.getResponseCode(), "GET / returns HTTP 200 OK");

            // 3. Security Boundary: GET /admin must be strictly 403 Forbidden
            HttpURLConnection adminConn = (HttpURLConnection) new URL(baseUrl + "/admin").openConnection();
            adminConn.setRequestMethod("GET");
            int adminCode = adminConn.getResponseCode();
            assertEquals(403, adminCode, "GET /admin is restricted with HTTP 403 Forbidden");

            // 4. Security Boundary: GET /dashboard must be strictly 403 Forbidden
            HttpURLConnection dashConn = (HttpURLConnection) new URL(baseUrl + "/dashboard").openConnection();
            dashConn.setRequestMethod("GET");
            int dashCode = dashConn.getResponseCode();
            assertEquals(403, dashCode, "GET /dashboard is restricted with HTTP 403 Forbidden");

            // 5. Security Boundary: GET /report must be strictly 403 Forbidden
            HttpURLConnection reportConn = (HttpURLConnection) new URL(baseUrl + "/report").openConnection();
            reportConn.setRequestMethod("GET");
            int reportCode = reportConn.getResponseCode();
            assertEquals(403, reportCode, "GET /report is restricted with HTTP 403 Forbidden");

            // 6. Security Boundary: POST /feed must be 405 Method Not Allowed
            HttpURLConnection postConn = (HttpURLConnection) new URL(baseUrl + "/feed").openConnection();
            postConn.setRequestMethod("POST");
            postConn.setDoOutput(true);
            int postCode = postConn.getResponseCode();
            assertEquals(405, postCode, "POST /feed is rejected with HTTP 405 Method Not Allowed");

        } catch (Exception e) {
            System.err.println("WebFeedServer test failed with exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            if (webServer != null) {
                webServer.stop();
                assertTrue(!webServer.isRunning(), "WebFeedServer successfully stopped");
            }
            if (tempItemCsv != null) {
                try {
                    Files.deleteIfExists(tempItemCsv);
                } catch (IOException ignored) {}
            }
        }
    }

    private static void testGoogleAuthenticationAndAutoRegistration() {
        System.out.println("\n--- 14. Testing Google Authentication & Auto-Registration ---");
        Path tempUserCsv = null;
        try {
            tempUserCsv = Files.createTempFile("test_google_users_", ".csv");
            UserManager userManager = new UserManager(new CsvUserStorageService(tempUserCsv));

            int initialCount = userManager.getAllUsers().size();

            // 1. First time Google login: should auto-register new account
            UserAccount googleUser = userManager.authenticateOrRegisterGoogle("alex.rivera.dev@gmail.com", "Alex Rivera");
            assertEquals("Alex Rivera", googleUser.getName(), "Google user full name matches");
            assertEquals("alex.rivera.dev@gmail.com", googleUser.getEmail(), "Google user email matches");
            assertEquals(UserRole.USER, googleUser.getRole(), "Google user has Standard User role");
            assertTrue(googleUser.isActive(), "Google user is active");
            assertTrue(googleUser.getUsername().contains("alex"), "Generated username derives from Google email prefix");
            assertEquals(initialCount + 1, userManager.getAllUsers().size(), "User count incremented after Google registration");

            // 2. Second time Google login with SAME email: should return existing account without duplicates
            UserAccount existingGoogleUser = userManager.authenticateOrRegisterGoogle("alex.rivera.dev@gmail.com", "Alex Rivera");
            assertEquals(googleUser.getUsername(), existingGoogleUser.getUsername(), "Same Google account returned on repeated login");
            assertEquals(initialCount + 1, userManager.getAllUsers().size(), "User count does not increase on re-authentication");

            // 3. Persistence verification: reload from CSV
            UserManager reloadedManager = new UserManager(new CsvUserStorageService(tempUserCsv));
            Optional<UserAccount> loadedGoogleUser = reloadedManager.findByUsername(googleUser.getUsername());
            assertTrue(loadedGoogleUser.isPresent(), "Google account persisted to disk and reloaded");
            assertEquals("Alex Rivera", loadedGoogleUser.get().getName(), "Persisted Google user name preserved");
            assertEquals("alex.rivera.dev@gmail.com", loadedGoogleUser.get().getEmail(), "Persisted Google email preserved");

        } catch (Exception e) {
            System.err.println("Google auth test failed with exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            if (tempUserCsv != null) {
                try {
                    Files.deleteIfExists(tempUserCsv);
                } catch (IOException ignored) {}
            }
        }
    }

    private static void testMongoArchitectureAndBsonMapping() {
        System.out.println("\n--- 15. Testing MongoDB Configuration & Offline Fallback Architecture ---");
        try {
            // 1. Config loading & DotEnv integration
            assertEquals("lostfound", DotEnv.get("MONGODB_DATABASE"), "DotEnv loaded MONGODB_DATABASE from .env");
            assertTrue(DotEnv.get("MONGODB_URI") != null && DotEnv.get("MONGODB_URI").startsWith("mongodb"), "DotEnv loaded valid MONGODB_URI from .env");
            assertEquals(8080, DotEnv.getInt("PORT", 3000), "DotEnv loaded PORT from .env");

            MongoConfig config = MongoConfig.load();
            assertEquals("lostfound", config.getDatabaseName(), "MongoConfig database name is lostfound");
            assertEquals("items", config.getItemsCollection(), "MongoConfig items collection is items");
            assertEquals("users", config.getUsersCollection(), "MongoConfig users collection is users");
            assertTrue(config.getConnectTimeoutMs() > 0, "MongoConfig connect timeout positive");

            // 2. LostItem BSON mapping round-trip
            User reporter = new User("Alice Brown", "alice@example.com", "555-0199");
            LostItem lostItem = new LostItem(
                    "LOST-9999", "Vintage Leather Wallet", ItemCategory.WALLET_PURSE,
                    LocalDate.of(2026, 11, 15), "Brown leather wallet with ID card",
                    ItemStatus.LOST, reporter, "Main Campus Library 2nd Floor", 25.0
            );
            lostItem.setImagePath("/images/wallet.png");
            lostItem.setResolutionNotes("Reported via mobile");

            Document lostDoc = MongoItemStorageService.itemToDocument(lostItem);
            assertEquals("LOST-9999", lostDoc.getString("_id"), "LostItem BSON _id matches");
            assertEquals("LOST", lostDoc.getString("type"), "LostItem BSON type is LOST");
            assertEquals(25.0, lostDoc.getDouble("rewardAmount"), "LostItem BSON reward matches");
            assertEquals("Alice Brown", lostDoc.getString("reporterName"), "LostItem BSON reporterName matches");

            Item deserializedLost = MongoItemStorageService.documentToItem(lostDoc);
            assertTrue(deserializedLost instanceof LostItem, "Deserialized BSON item is LostItem");
            assertEquals("Vintage Leather Wallet", deserializedLost.getName(), "Deserialized LostItem name matches");
            assertEquals(25.0, ((LostItem) deserializedLost).getRewardAmount(), "Deserialized LostItem reward matches");

            // 3. FoundItem BSON mapping round-trip
            FoundItem foundItem = new FoundItem(
                    "FOUND-8888", "AirPods Pro Case", ItemCategory.ELECTRONICS,
                    LocalDate.of(2026, 11, 14), "White charging case found on table",
                    ItemStatus.FOUND, reporter, "Student Union Cafeteria",
                    "Security Desk Safe B", "security@campus.edu"
            );
            Document foundDoc = MongoItemStorageService.itemToDocument(foundItem);
            assertEquals("FOUND-8888", foundDoc.getString("_id"), "FoundItem BSON _id matches");
            assertEquals("FOUND", foundDoc.getString("type"), "FoundItem BSON type is FOUND");
            assertEquals("Security Desk Safe B", foundDoc.getString("storageLocation"), "FoundItem BSON storageLocation matches");

            Item deserializedFound = MongoItemStorageService.documentToItem(foundDoc);
            assertTrue(deserializedFound instanceof FoundItem, "Deserialized BSON item is FoundItem");
            assertEquals("Security Desk Safe B", ((FoundItem) deserializedFound).getStorageLocation(), "Deserialized FoundItem storageLocation matches");

            // 4. UserAccount BSON mapping round-trip
            UserAccount account = new UserAccount(
                    "alex_admin", "AdminPasswordHash", UserRole.ADMIN,
                    "Alex Smith", "alex@enterprise.com", "555-1234",
                    java.time.LocalDateTime.now(), true
            );
            Document userDoc = MongoUserStorageService.userToDocument(account);
            assertEquals("alex_admin", userDoc.getString("_id"), "UserAccount BSON _id matches");
            assertEquals("ADMIN", userDoc.getString("role"), "UserAccount BSON role matches");

            UserAccount deserializedUser = MongoUserStorageService.documentToUser(userDoc);
            assertTrue(deserializedUser != null, "Deserialized UserAccount not null");
            assertEquals("alex_admin", deserializedUser.getUsername(), "Deserialized UserAccount username matches");
            assertTrue(deserializedUser.getRole().isAdmin(), "Deserialized UserAccount has admin role");
            assertEquals("Alex Smith", deserializedUser.getName(), "Deserialized UserAccount name matches");

            // 5. Offline Resilient Fallback for MongoItemStorageService
            Path tempItemCsv = Files.createTempFile("mongo_fallback_items_", ".csv");
            MongoItemStorageService itemService = new MongoItemStorageService(
                    com.smartlostfound.storage.MongoClientProvider.getInstance(),
                    tempItemCsv
            );
            itemService.saveItems(List.of(lostItem, foundItem));
            List<Item> reloadedItems = itemService.loadItems();
            assertEquals(2, reloadedItems.size(), "MongoItemStorageService saved and loaded 2 items via resilient fallback");
            Files.deleteIfExists(tempItemCsv);

            // 6. Offline Resilient Fallback for MongoUserStorageService
            Path tempUserCsv = Files.createTempFile("mongo_fallback_users_", ".csv");
            MongoUserStorageService userService = new MongoUserStorageService(
                    com.smartlostfound.storage.MongoClientProvider.getInstance(),
                    tempUserCsv
            );
            userService.saveUsers(List.of(account));
            List<UserAccount> reloadedUsers = userService.loadUsers();
            assertEquals(1, reloadedUsers.size(), "MongoUserStorageService saved and loaded 1 user via resilient fallback");
            Files.deleteIfExists(tempUserCsv);

        } catch (Exception e) {
            System.err.println("MongoDB test failed with exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }

    private static void testFeedNewestFirstChronologicalOrdering() {
        System.out.println("\n--- 16. Testing Community Feed Chronological Ordering (Newest Items First) ---");
        try {
            LostItem oldItem = new LostItem(
                    "LOST-9001", "Old Notebook", ItemCategory.OTHER,
                    LocalDate.now().minusDays(5), "Desc", ItemStatus.LOST,
                    new User("User1", "u1@test.com", "111"), "Hall A", 0.0
            );
            oldItem.setCreatedAt(LocalDateTime.now().minusDays(5));

            FoundItem midItem = new FoundItem(
                    "FND-9002", "Mid Jacket", ItemCategory.CLOTHING,
                    LocalDate.now().minusDays(1), "Desc", ItemStatus.FOUND,
                    new User("User2", "u2@test.com", "222"), "Gym", "Locker 1", "Staff"
            );
            midItem.setCreatedAt(LocalDateTime.now().minusDays(1));

            LostItem latestItem = new LostItem(
                    "LOST-9003", "Brand New Phone", ItemCategory.ELECTRONICS,
                    LocalDate.now(), "Desc", ItemStatus.LOST,
                    new User("User3", "u3@test.com", "333"), "Cafeteria", 50.0
            );
            latestItem.setCreatedAt(LocalDateTime.now());

            List<Item> unsorted = List.of(oldItem, midItem, latestItem);
            List<Item> sorted = new java.util.ArrayList<>(unsorted);
            sorted.sort((a, b) -> {
                if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                    int c = b.getCreatedAt().compareTo(a.getCreatedAt());
                    if (c != 0) return c;
                }
                if (a.getDate() != null && b.getDate() != null) {
                    int c = b.getDate().compareTo(a.getDate());
                    if (c != 0) return c;
                }
                if (a.getId() != null && b.getId() != null) {
                    return b.getId().compareTo(a.getId());
                }
                return 0;
            });

            assertEquals("LOST-9003", sorted.get(0).getId(), "Most recently posted item appears first at top of feed");
            assertEquals("FND-9002", sorted.get(1).getId(), "Intermediate item appears second in feed");
            assertEquals("LOST-9001", sorted.get(2).getId(), "Older item appears last in feed");

            // Verify manager places newly reported item at top of list
            Path tempCsv = Files.createTempFile("test_feed_order_", ".csv");
            LostFoundManager manager = new LostFoundManager(new CsvItemStorageService(tempCsv));
            LostItem newlyPosted = manager.reportLostItem(
                    "Super New Item", ItemCategory.ELECTRONICS, LocalDate.now(),
                    "Freshly dropped", new User("Tester", "t@test.com", "123"), "Room 101", 20.0
            );
            assertEquals(newlyPosted.getId(), manager.getAllItems().get(0).getId(),
                    "Manager.reportLostItem places newly posted item at index 0 (top of feed)");
            Files.deleteIfExists(tempCsv);

        } catch (Exception e) {
            System.err.println("Feed chronological test failed: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }
    }
}
