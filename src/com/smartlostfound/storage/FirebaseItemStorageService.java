package com.smartlostfound.storage;

import com.smartlostfound.model.Item;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Hybrid Cloud Storage implementation for Lost and Found items.
 * Uses Firebase Realtime Database as the primary cloud data source,
 * with automatic synchronization and seamless local CSV caching as fallback.
 */
public class FirebaseItemStorageService implements ItemStorageService {

    private final FirebaseRealtimeClient client;
    private final CsvItemStorageService localFallback;
    private boolean isCloudConnected = false;

    public FirebaseItemStorageService() {
        this(FirebaseConfig.load(), Paths.get("data/items.csv"));
    }

    public FirebaseItemStorageService(FirebaseConfig config, Path localFallbackPath) {
        this.client = new FirebaseRealtimeClient(config);
        this.localFallback = new CsvItemStorageService(localFallbackPath);
    }

    @Override
    public List<Item> loadItems() throws DataPersistenceException {
        try {
            String json = client.get("items");
            isCloudConnected = true;
            if (json != null && !json.trim().isEmpty()) {
                List<Item> items = FirebaseJsonParser.parseItems(json);
                if (!items.isEmpty()) {
                    // Update local offline cache
                    try {
                        localFallback.saveItems(items);
                    } catch (Exception ignored) {}
                    return items;
                }
            }

            // Cloud is empty: seed cloud from local cache if available
            if (localFallback.storageExists()) {
                List<Item> localItems = localFallback.loadItems();
                if (!localItems.isEmpty()) {
                    try {
                        String payload = FirebaseJsonParser.serializeItems(localItems);
                        client.put("items", payload);
                    } catch (Exception ignored) {}
                    return localItems;
                }
            }
            return List.of();
        } catch (IOException | InterruptedException e) {
            isCloudConnected = false;
            System.err.println("Notice: Firebase cloud unavailable (" + e.getMessage() + "), using local cache.");
            return localFallback.loadItems();
        }
    }

    @Override
    public void saveItems(List<Item> items) throws DataPersistenceException {
        // 1. Always update local cache first (ensures resilience against network drop)
        localFallback.saveItems(items);

        // 2. Synchronize to Firebase Cloud
        try {
            String payload = FirebaseJsonParser.serializeItems(items);
            client.put("items", payload);
            isCloudConnected = true;
        } catch (Exception e) {
            isCloudConnected = false;
            System.err.println("Notice: Failed to push to Firebase cloud (" + e.getMessage() + "). Saved to local cache.");
        }
    }

    @Override
    public String getStoragePath() {
        return client.getConfig().getDatabaseUrl() + "/items.json (" + (isCloudConnected ? "Cloud Active" : "Local Cache") + ")";
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
