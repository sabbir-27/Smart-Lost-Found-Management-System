package com.smartlostfound.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration holder for MongoDB database connectivity.
 * Reads properties from data/mongodb-config.json with environment variable overrides
 * (e.g. MONGODB_URI or MONGODB_DATABASE).
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
        this.itemsCollection = (itemsCollection != null && !itemsCollection.isEmpty()) ? itemsCollection : DEFAULT_ITEMS_COLLECTION;
        this.usersCollection = (usersCollection != null && !usersCollection.isEmpty()) ? usersCollection : DEFAULT_USERS_COLLECTION;
        this.connectTimeoutMs = connectTimeoutMs > 0 ? connectTimeoutMs : 3000;
        this.serverSelectionTimeoutMs = serverSelectionTimeoutMs > 0 ? serverSelectionTimeoutMs : 3000;
    }

    private static String resolveUri(String candidate) {
        String envUri = System.getenv("MONGODB_URI");
        if (envUri != null && !envUri.trim().isEmpty()) {
            return envUri.trim();
        }
        String propUri = System.getProperty("mongodb.uri");
        if (propUri != null && !propUri.trim().isEmpty()) {
            return propUri.trim();
        }
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate.trim() : DEFAULT_URI;
    }

    private static String resolveDatabase(String candidate) {
        String envDb = System.getenv("MONGODB_DATABASE");
        if (envDb != null && !envDb.trim().isEmpty()) {
            return envDb.trim();
        }
        return (candidate != null && !candidate.trim().isEmpty()) ? candidate.trim() : DEFAULT_DATABASE;
    }

    public static MongoConfig load() {
        return load(Paths.get(DEFAULT_CONFIG_PATH));
    }

    public static MongoConfig load(Path configPath) {
        if (configPath != null && Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                String uri = extractString(content, "connectionUri");
                String db = extractString(content, "databaseName");
                String itemsCol = extractString(content, "itemsCollection");
                String usersCol = extractString(content, "usersCollection");
                int connTimeout = extractInt(content, "connectTimeoutMs", 3000);
                int selTimeout = extractInt(content, "serverSelectionTimeoutMs", 3000);
                return new MongoConfig(uri, db, itemsCol, usersCol, connTimeout, selTimeout);
            } catch (IOException e) {
                System.err.println("Notice: Could not parse mongodb-config.json, using defaults: " + e.getMessage());
            }
        }
        return new MongoConfig(DEFAULT_URI, DEFAULT_DATABASE, DEFAULT_ITEMS_COLLECTION, DEFAULT_USERS_COLLECTION, 3000, 3000);
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
