package com.smartlostfound.storage;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.smartlostfound.model.*;
import org.bson.Document;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise MongoDB Storage Service for Lost &amp; Found Items.
 * Stores item records in a MongoDB collection (e.g. lostfound.items)
 * with robust local CSV caching for offline resilience.
 */
public class MongoItemStorageService implements ItemStorageService {

    private final MongoClientProvider clientProvider;
    private final MongoConfig config;
    private final CsvItemStorageService localFallback;
    private boolean isMongoActive = false;

    public MongoItemStorageService() {
        this(MongoClientProvider.getInstance(), Paths.get("data/items.csv"));
    }

    public MongoItemStorageService(MongoClientProvider clientProvider, Path localFallbackPath) {
        this.clientProvider = clientProvider;
        this.config = clientProvider.getConfig();
        this.localFallback = new CsvItemStorageService(localFallbackPath);
    }

    @Override
    public List<Item> loadItems() throws DataPersistenceException {
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getItemsCollection());
                FindIterable<Document> docs = col.find();
                List<Item> items = new ArrayList<>();

                for (Document doc : docs) {
                    Item item = documentToItem(doc);
                    if (item != null) {
                        items.add(item);
                    }
                }

                if (!items.isEmpty()) {
                    isMongoActive = true;
                    // Sync to local cache
                    try {
                        localFallback.saveItems(items);
                    } catch (Exception ignored) {}
                    return items;
                }

                // If MongoDB collection is empty, seed from local storage if available
                if (localFallback.storageExists()) {
                    List<Item> localSeed = localFallback.loadItems();
                    if (!localSeed.isEmpty()) {
                        saveItemsToMongo(col, localSeed);
                        isMongoActive = true;
                        return localSeed;
                    }
                }
                isMongoActive = true;
                return items;
            } catch (Exception e) {
                System.err.println("Notice: MongoDB read failed (" + e.getMessage() + "), falling back to local cache.");
                isMongoActive = false;
            }
        } else {
            isMongoActive = false;
            System.err.println("Notice: MongoDB (" + config.getConnectionUri() + ") unavailable, running in local cache mode.");
        }

        return localFallback.loadItems();
    }

    @Override
    public void saveItems(List<Item> items) throws DataPersistenceException {
        // 1. Always update local cache first (guarantees zero data loss)
        localFallback.saveItems(items);

        // 2. Synchronize to MongoDB if available
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getItemsCollection());
                saveItemsToMongo(col, items);
                isMongoActive = true;
            } catch (Exception e) {
                isMongoActive = false;
                System.err.println("Notice: Failed to sync items to MongoDB (" + e.getMessage() + "). Saved to local cache.");
            }
        }
    }

    private void saveItemsToMongo(MongoCollection<Document> col, List<Item> items) {
        ReplaceOptions upsertOpt = new ReplaceOptions().upsert(true);
        for (Item item : items) {
            Document doc = itemToDocument(item);
            col.replaceOne(Filters.eq("_id", item.getId()), doc, upsertOpt);
        }
    }

    public static Document itemToDocument(Item item) {
        Document doc = new Document("_id", item.getId())
                .append("name", item.getName())
                .append("type", item.getItemType().toUpperCase())
                .append("category", item.getCategory() != null ? item.getCategory().name() : "OTHER")
                .append("date", item.getDate() != null ? item.getDate().toString() : LocalDate.now().toString())
                .append("description", item.getDescription() != null ? item.getDescription() : "")
                .append("status", item.getStatus() != null ? item.getStatus().name() : "LOST")
                .append("specificLocation", item.getSpecificLocation() != null ? item.getSpecificLocation() : "")
                .append("resolutionNotes", item.getResolutionNotes() != null ? item.getResolutionNotes() : "")
                .append("imagePath", item.getImagePath() != null ? item.getImagePath() : "");

        User reporter = item.getReporter();
        if (reporter != null) {
            doc.append("reporterName", reporter.getName())
               .append("reporterEmail", reporter.getEmail())
               .append("reporterPhone", reporter.getPhone());
        }

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            doc.append("rewardAmount", lost.getRewardAmount());
        } else if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            doc.append("storageLocation", found.getStorageLocation())
               .append("finderContactInfo", found.getFinderContactInfo());
        }

        return doc;
    }

    public static Item documentToItem(Document doc) {
        try {
            String id = doc.getString("_id");
            String name = doc.getString("name");
            String type = doc.getString("type");
            String catStr = doc.getString("category");
            ItemCategory category = ItemCategory.fromString(catStr);

            LocalDate date = LocalDate.now();
            String dateStr = doc.getString("date");
            if (dateStr != null && !dateStr.isEmpty()) {
                try { date = LocalDate.parse(dateStr); } catch (Exception ignored) {}
            }

            String description = doc.getString("description");
            ItemStatus status = ItemStatus.fromString(doc.getString("status"));
            String location = doc.getString("specificLocation");

            User reporter = new User(
                    doc.getString("reporterName"),
                    doc.getString("reporterEmail"),
                    doc.getString("reporterPhone")
            );

            String resolution = doc.getString("resolutionNotes");
            String imagePath = doc.getString("imagePath");

            if ("FOUND".equalsIgnoreCase(type) || (id != null && id.startsWith("FND"))) {
                String storage = doc.getString("storageLocation");
                String contact = doc.getString("finderContactInfo");
                FoundItem f = new FoundItem(id, name, category, date, description, status,
                        reporter, location, storage, contact != null ? contact : "");
                f.setResolutionNotes(resolution);
                f.setImagePath(imagePath);
                return f;
            } else {
                Number rewardNum = doc.get("rewardAmount", Number.class);
                double reward = rewardNum != null ? rewardNum.doubleValue() : 0.0;
                LostItem l = new LostItem(id, name, category, date, description, status,
                        reporter, location, reward);
                l.setResolutionNotes(resolution);
                l.setImagePath(imagePath);
                return l;
            }
        } catch (Exception e) {
            System.err.println("Warning: Error converting MongoDB document to Item: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String getStoragePath() {
        return "MongoDB [" + config.getConnectionUri() + "/" + config.getDatabaseName() + "." + config.getItemsCollection()
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
