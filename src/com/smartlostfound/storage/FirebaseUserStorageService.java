package com.smartlostfound.storage;

import com.smartlostfound.model.UserAccount;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Hybrid Cloud Storage implementation for User and Administrator accounts.
 * Connects to Firebase Realtime Database (/users.json) with automatic local CSV caching.
 */
public class FirebaseUserStorageService implements UserStorageService {

    private final FirebaseRealtimeClient client;
    private final CsvUserStorageService localFallback;
    private boolean isCloudConnected = false;

    public FirebaseUserStorageService() {
        this(FirebaseConfig.load(), Paths.get("data/users.csv"));
    }

    public FirebaseUserStorageService(FirebaseConfig config, Path localFallbackPath) {
        this.client = new FirebaseRealtimeClient(config);
        this.localFallback = new CsvUserStorageService(localFallbackPath);
    }

    @Override
    public List<UserAccount> loadUsers() throws DataPersistenceException {
        try {
            String json = client.get("users");
            isCloudConnected = true;
            if (json != null && !json.trim().isEmpty()) {
                List<UserAccount> users = FirebaseJsonParser.parseUsers(json);
                if (!users.isEmpty()) {
                    try {
                        localFallback.saveUsers(users);
                    } catch (Exception ignored) {}
                    return users;
                }
            }

            // Cloud is empty: seed cloud from local users if available
            if (localFallback.storageExists()) {
                List<UserAccount> localUsers = localFallback.loadUsers();
                if (!localUsers.isEmpty()) {
                    try {
                        String payload = FirebaseJsonParser.serializeUsers(localUsers);
                        client.put("users", payload);
                    } catch (Exception ignored) {}
                    return localUsers;
                }
            }
            return List.of();
        } catch (IOException | InterruptedException e) {
            isCloudConnected = false;
            System.err.println("Notice: Firebase user cloud unavailable (" + e.getMessage() + "), using local cache.");
            return localFallback.loadUsers();
        }
    }

    @Override
    public void saveUsers(List<UserAccount> users) throws DataPersistenceException {
        // 1. Always update local cache first
        localFallback.saveUsers(users);

        // 2. Synchronize to Firebase Cloud
        try {
            String payload = FirebaseJsonParser.serializeUsers(users);
            client.put("users", payload);
            isCloudConnected = true;
        } catch (Exception e) {
            isCloudConnected = false;
            System.err.println("Notice: Failed to push users to Firebase cloud (" + e.getMessage() + "). Saved to local cache.");
        }
    }

    @Override
    public String getStoragePath() {
        return client.getConfig().getDatabaseUrl() + "/users.json (" + (isCloudConnected ? "Cloud Active" : "Local Cache") + ")";
    }

    @Override
    public boolean storageExists() {
        return true;
    }

    public boolean isCloudConnected() {
        return isCloudConnected;
    }

    public FirebaseRealtimeClient getClient() {
        return client;
    }
}
