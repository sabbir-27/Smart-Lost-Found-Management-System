package com.smartlostfound.storage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;
import org.bson.Document;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise MongoDB Storage Service for User and Administrator Accounts.
 * Stores user records in a MongoDB collection (e.g. lostfound.users)
 * with robust local CSV caching for authentication persistence.
 */
public class MongoUserStorageService implements UserStorageService {

    private final MongoClientProvider clientProvider;
    private final MongoConfig config;
    private final CsvUserStorageService localFallback;
    private boolean isMongoActive = false;

    public MongoUserStorageService() {
        this(MongoClientProvider.getInstance(), Paths.get("data/users.csv"));
    }

    public MongoUserStorageService(MongoClientProvider clientProvider, Path localFallbackPath) {
        this.clientProvider = clientProvider;
        this.config = clientProvider.getConfig();
        this.localFallback = new CsvUserStorageService(localFallbackPath);
    }

    @Override
    public List<UserAccount> loadUsers() throws DataPersistenceException {
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getUsersCollection());
                FindIterable<Document> docs = col.find();
                List<UserAccount> users = new ArrayList<>();

                for (Document doc : docs) {
                    UserAccount user = documentToUser(doc);
                    if (user != null) {
                        users.add(user);
                    }
                }

                if (!users.isEmpty()) {
                    isMongoActive = true;
                    // Sync to local cache
                    try {
                        localFallback.saveUsers(users);
                    } catch (Exception ignored) {}
                    return users;
                }

                // If collection empty, seed from local CSV if present
                if (localFallback.storageExists()) {
                    List<UserAccount> localUsers = localFallback.loadUsers();
                    if (!localUsers.isEmpty()) {
                        saveUsersToMongo(col, localUsers);
                        isMongoActive = true;
                        return localUsers;
                    }
                }
                isMongoActive = true;
                return users;
            } catch (Exception e) {
                System.err.println("Notice: MongoDB user read failed (" + e.getMessage() + "), using local cache.");
                isMongoActive = false;
            }
        } else {
            isMongoActive = false;
        }

        return localFallback.loadUsers();
    }

    @Override
    public void saveUsers(List<UserAccount> users) throws DataPersistenceException {
        // 1. Always update local cache first
        localFallback.saveUsers(users);

        // 2. Synchronize to MongoDB if available
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getUsersCollection());
                saveUsersToMongo(col, users);
                isMongoActive = true;
            } catch (Exception e) {
                isMongoActive = false;
                System.err.println("Notice: Failed to sync users to MongoDB (" + e.getMessage() + "). Saved to local cache.");
            }
        }
    }

    private void saveUsersToMongo(MongoCollection<Document> col, List<UserAccount> users) {
        ReplaceOptions upsertOpt = new ReplaceOptions().upsert(true);
        for (UserAccount user : users) {
            Document doc = userToDocument(user);
            col.replaceOne(Filters.eq("_id", user.getUsername().toLowerCase()), doc, upsertOpt);
        }
    }

    public static Document userToDocument(UserAccount user) {
        return new Document("_id", user.getUsername().toLowerCase())
                .append("username", user.getUsername())
                .append("passwordHash", user.getPasswordHash())
                .append("role", user.getRole() != null ? user.getRole().name() : "USER")
                .append("name", user.getName())
                .append("email", user.getEmail())
                .append("phone", user.getPhone())
                .append("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString())
                .append("active", user.isActive());
    }

    public static UserAccount documentToUser(Document doc) {
        try {
            String username = doc.getString("username");
            if (username == null || username.trim().isEmpty()) {
                username = doc.getString("_id");
            }
            if (username == null || username.trim().isEmpty()) return null;

            String passHash = doc.getString("passwordHash");
            String roleStr = doc.getString("role");
            UserRole role = "ADMIN".equalsIgnoreCase(roleStr) ? UserRole.ADMIN : UserRole.USER;

            String name = doc.getString("name");
            String email = doc.getString("email");
            String phone = doc.getString("phone");

            LocalDateTime createdAt = LocalDateTime.now();
            String createdStr = doc.getString("createdAt");
            if (createdStr != null && !createdStr.isEmpty()) {
                try { createdAt = LocalDateTime.parse(createdStr); } catch (Exception ignored) {}
            }

            Boolean activeVal = doc.getBoolean("active");
            boolean active = activeVal != null ? activeVal : true;

            return new UserAccount(username, passHash, role, name, email, phone, createdAt, active);
        } catch (Exception e) {
            System.err.println("Warning: Error converting MongoDB document to UserAccount: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getStoragePath() {
        return "MongoDB [" + config.getConnectionUri() + "/" + config.getDatabaseName() + "." + config.getUsersCollection()
                + "] (" + (isMongoActive ? "Active" : "Local Cache Fallback") + ")";
    }

    @Override
    public boolean storageExists() {
        return true;
    }

    public boolean isMongoActive() {
        return isMongoActive;
    }
}
