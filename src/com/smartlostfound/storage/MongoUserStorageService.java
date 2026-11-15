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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MongoDB-backed user storage service with resilient local CSV fallback.
 * Manages user accounts, authentication hashes, and roles.
 */
public class MongoUserStorageService implements UserStorageService {

    private static final Path DEFAULT_FALLBACK_PATH = Paths.get("data", "users.csv");

    private final MongoClientProvider clientProvider;
    private final MongoConfig config;
    private final CsvUserStorageService localFallback;
    private boolean isMongoActive = false;

    public MongoUserStorageService() {
        this(MongoClientProvider.getInstance(), DEFAULT_FALLBACK_PATH);
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
                isMongoActive = false;
            }
        } else {
            isMongoActive = false;
        }

        return localFallback.loadUsers();
    }

    @Override
    public void saveUsers(List<UserAccount> users) throws DataPersistenceException {
        // Guarantee local persistence first
        localFallback.saveUsers(users);

        // Sync to MongoDB if connected
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getUsersCollection());
                saveUsersToMongo(col, users);
                isMongoActive = true;
            } catch (Exception e) {
                isMongoActive = false;
            }
        }
    }

    private void saveUsersToMongo(MongoCollection<Document> col, List<UserAccount> users) {
        if (users == null || users.isEmpty()) {
            col.deleteMany(new Document());
            return;
        }

        ReplaceOptions upsertOpt = new ReplaceOptions().upsert(true);
        List<String> currentIds = new ArrayList<>();
        for (UserAccount user : users) {
            String id = user.getUsername().toLowerCase();
            currentIds.add(id);
            Document doc = userToDocument(user);
            col.replaceOne(Filters.eq("_id", id), doc, upsertOpt);
        }

        // Clean up any accounts deleted from the local session
        col.deleteMany(Filters.nin("_id", currentIds));
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
            Object rawCreated = doc.get("createdAt");
            if (rawCreated instanceof Date) {
                createdAt = ((Date) rawCreated).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            } else if (rawCreated != null) {
                String str = rawCreated.toString().trim();
                if (!str.isEmpty()) {
                    try { createdAt = LocalDateTime.parse(str); } catch (Exception ignored) {}
                }
            }

            Object rawActive = doc.get("active");
            boolean active = true;
            if (rawActive instanceof Boolean) {
                active = (Boolean) rawActive;
            } else if (rawActive != null) {
                active = Boolean.parseBoolean(rawActive.toString().trim());
            }

            return new UserAccount(username, passHash, role, name, email, phone, createdAt, active);
        } catch (Exception e) {
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
