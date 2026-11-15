package com.smartlostfound.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration holder for MongoDB database connectivity.
 * Reads properties with highest priority from the .env configuration file
 * or system environment variables, with optional fallback to data/mongodb-config.json.
 */
public class MongoConfig {

    private static final String DEFAULT_CONFIG_PATH = "data/mongodb-config.json";
    private static final String DEFAULT_URI = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE = "lostfound";
    private static final String DEFAULT_ITEMS_COLLECTION = "items";
    private static final String DEFAULT_USERS_COLLECTION = "users";

    private final String connectionUri;
    private final String databaseName;
    private final String itemsCollection;
    private final String usersCollection;
    private final int connectTimeoutMs;
    private final int serverSelectionTimeoutMs;

    public MongoConfig(String connectionUri, String databaseName, String itemsCollection,
                       String usersCollection, int connectTimeoutMs, int serverSelectionTimeoutMs) {
        this.connectionUri = resolveUri(connectionUri);
        this.databaseName = resolveDatabase(databaseName);
        this.itemsCollection = (itemsCollection != null && !itemsCollection.isEmpty())
                ? itemsCollection : DotEnv.get("MONGODB_ITEMS_COLLECTION", DEFAULT_ITEMS_COLLECTION);
        this.usersCollection = (usersCollection != null && !usersCollection.isEmpty())
                ? usersCollection : DotEnv.get("MONGODB_USERS_COLLECTION", DEFAULT_USERS_COLLECTION);
        this.connectTimeoutMs = connectTimeoutMs > 0
                ? connectTimeoutMs : DotEnv.getInt("MONGODB_CONNECT_TIMEOUT_MS", 3000);
        this.serverSelectionTimeoutMs = serverSelectionTimeoutMs > 0
                ? serverSelectionTimeoutMs : DotEnv.getInt("MONGODB_SERVER_SELECTION_TIMEOUT_MS", 3000);
    }

    private static String resolveUri(String candidate) {
        String uri = DotEnv.get("MONGODB_URI");
        if (uri != null && !uri.trim().isEmpty()) {
            return uri.trim();
        }
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate.trim() : DEFAULT_URI;
    }

    private static String resolveDatabase(String candidate) {
        String db = DotEnv.get("MONGODB_DATABASE");
        if (db != null && !db.trim().isEmpty()) {
            return db.trim();
        }
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate.trim() : DEFAULT_DATABASE;
    }

    public static MongoConfig load() {
        return load(Paths.get(DEFAULT_CONFIG_PATH));
    }

    public static MongoConfig load(Path configPath) {
        String uri = DotEnv.get("MONGODB_URI");
        String db = DotEnv.get("MONGODB_DATABASE");
        String itemsCol = DotEnv.get("MONGODB_ITEMS_COLLECTION");
        String usersCol = DotEnv.get("MONGODB_USERS_COLLECTION");
        int connTimeout = DotEnv.getInt("MONGODB_CONNECT_TIMEOUT_MS", -1);
        int selTimeout = DotEnv.getInt("MONGODB_SERVER_SELECTION_TIMEOUT_MS", -1);

        if (configPath != null && Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                if (uri == null) uri = extractString(content, "connectionUri");
                if (db == null) db = extractString(content, "databaseName");
                if (itemsCol == null) itemsCol = extractString(content, "itemsCollection");
                if (usersCol == null) usersCol = extractString(content, "usersCollection");
                if (connTimeout <= 0) connTimeout = extractInt(content, "connectTimeoutMs", 3000);
                if (selTimeout <= 0) selTimeout = extractInt(content, "serverSelectionTimeoutMs", 3000);
            } catch (IOException e) {
                System.err.println("Notice: Could not parse config file, using environment defaults: " + e.getMessage());
            }
        }

        return new MongoConfig(
                uri != null ? uri : DEFAULT_URI,
                db != null ? db : DEFAULT_DATABASE,
                itemsCol != null ? itemsCol : DEFAULT_ITEMS_COLLECTION,
                usersCol != null ? usersCol : DEFAULT_USERS_COLLECTION,
                connTimeout > 0 ? connTimeout : 3000,
                selTimeout > 0 ? selTimeout : 3000
        );
    }

    private static String extractString(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return null;
        int q1 = json.indexOf("\"", colon);
        if (q1 == -1) return null;
        int q2 = json.indexOf("\"", q1 + 1);
        if (q2 == -1) return null;
        return json.substring(q1 + 1, q2).trim();
    }

    private static int extractInt(String json, String key, int fallback) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return fallback;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return fallback;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (Exception e) {
            return fallback;
        }
    }

    public String getConnectionUri() {
        return connectionUri;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getItemsCollection() {
        return itemsCollection;
    }

    public String getUsersCollection() {
        return usersCollection;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getServerSelectionTimeoutMs() {
        return serverSelectionTimeoutMs;
    }
}
