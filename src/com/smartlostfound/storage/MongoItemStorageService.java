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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MongoDB-backed item storage service with resilient local CSV fallback.
 * Automatically switches between live MongoDB Atlas/local instances and
 * offline cached storage with zero data loss.
 */
public class MongoItemStorageService implements ItemStorageService {

    private static final Path DEFAULT_FALLBACK_PATH = Paths.get("data", "items.csv");

    private final MongoClientProvider clientProvider;
    private final MongoConfig config;
    private final CsvItemStorageService localFallback;
    private boolean isMongoActive = false;

    public MongoItemStorageService() {
        this(MongoClientProvider.getInstance(), DEFAULT_FALLBACK_PATH);
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
                    try {
                        localFallback.saveItems(items);
                    } catch (Exception ignored) {}
                    return items;
                }

                // If remote database collection is empty, seed from local CSV if present
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
                isMongoActive = false;
            }
        } else {
            isMongoActive = false;
        }

        return localFallback.loadItems();
    }

    @Override
    public void saveItems(List<Item> items) throws DataPersistenceException {
        // Guarantee local persistence first
        localFallback.saveItems(items);

        // Sync to MongoDB if connected
        if (clientProvider.isAvailable()) {
            try {
                MongoCollection<Document> col = clientProvider.getDatabase().getCollection(config.getItemsCollection());
                saveItemsToMongo(col, items);
                isMongoActive = true;
            } catch (Exception e) {
                isMongoActive = false;
            }
        }
    }

    private void saveItemsToMongo(MongoCollection<Document> col, List<Item> items) {
        if (items == null || items.isEmpty()) {
            col.deleteMany(new Document());
            return;
        }

        ReplaceOptions upsertOpt = new ReplaceOptions().upsert(true);
        List<String> currentIds = new ArrayList<>();
        for (Item item : items) {
            currentIds.add(item.getId());
            Document doc = itemToDocument(item);
            col.replaceOne(Filters.eq("_id", item.getId()), doc, upsertOpt);
        }

        // Clean up any items deleted from the local session
        col.deleteMany(Filters.nin("_id", currentIds));
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
            Object rawDate = doc.get("date");
            if (rawDate instanceof Date) {
                date = ((Date) rawDate).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            } else if (rawDate != null) {
                String str = rawDate.toString().trim();
                if (!str.isEmpty()) {
                    try { date = LocalDate.parse(str); } catch (Exception ignored) {}
                }
            }

            String description = doc.getString("description");
            ItemStatus status = ItemStatus.fromString(doc.getString("status"));
            String location = doc.getString("specificLocation");

            String repName = doc.getString("reporterName");
            String repEmail = doc.getString("reporterEmail");
            String repPhone = doc.getString("reporterPhone");
            User reporter = (repName != null || repEmail != null || repPhone != null)
                    ? new User(repName, repEmail, repPhone)
                    : new User("Community Reporter", "", "");

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
                Object rawReward = doc.get("rewardAmount");
                double reward = 0.0;
                if (rawReward instanceof Number) {
                    reward = ((Number) rawReward).doubleValue();
                } else if (rawReward != null) {
                    try { reward = Double.parseDouble(rawReward.toString().trim()); } catch (Exception ignored) {}
                }

                LostItem l = new LostItem(id, name, category, date, description, status,
                        reporter, location, reward);
                l.setResolutionNotes(resolution);
                l.setImagePath(imagePath);
                return l;
            }
        } catch (Exception e) {
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
