package com.smartlostfound.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration manager for Firebase Realtime Database and Cloud services.
 * Reads settings from data/firebase-config.json with graceful environment fallbacks.
 */
public class FirebaseConfig {

    private static final String DEFAULT_CONFIG_PATH = "data/firebase-config.json";
    private static final String DEFAULT_DATABASE_URL = "https://project-bc7fd1cc-3a38-4b61-a57-default-rtdb.firebaseio.com";
    private static final String DEFAULT_PROJECT_ID = "project-bc7fd1cc-3a38-4b61-a57";

    private final String projectId;
    private final String databaseUrl;
    private final String authToken;

    public FirebaseConfig(String projectId, String databaseUrl, String authToken) {
        this.projectId = (projectId != null && !projectId.trim().isEmpty()) ? projectId.trim() : DEFAULT_PROJECT_ID;
        this.databaseUrl = (databaseUrl != null && !databaseUrl.trim().isEmpty())
                ? databaseUrl.replaceAll("/+$", "")
                : DEFAULT_DATABASE_URL;
        this.authToken = (authToken != null) ? authToken.trim() : "";
    }

    public static FirebaseConfig load() {
        return load(Paths.get(DEFAULT_CONFIG_PATH));
    }

    public static FirebaseConfig load(Path configPath) {
        if (configPath != null && Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                String pId = extractJsonValue(content, "projectId");
                String dbUrl = extractJsonValue(content, "databaseUrl");
                String auth = extractJsonValue(content, "authToken");
                return new FirebaseConfig(pId, dbUrl, auth);
            } catch (IOException e) {
                System.err.println("Notice: Could not parse firebase-config.json, using defaults: " + e.getMessage());
            }
        }
        return new FirebaseConfig(DEFAULT_PROJECT_ID, DEFAULT_DATABASE_URL, "");
    }

    private static String extractJsonValue(String json, String key) {
        if (json == null) return null;
        int idx = json.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return null;
        int startQuote = json.indexOf("\"", colon);
        if (startQuote == -1) return null;
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (endQuote == -1) return null;
        return json.substring(startQuote + 1, endQuote).trim();
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean hasAuthToken() {
        return authToken != null && !authToken.isEmpty();
    }

    public String buildEndpoint(String path) {
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        if (!cleanPath.endsWith(".json")) {
            cleanPath += ".json";
        }
        String url = databaseUrl + "/" + cleanPath;
        if (hasAuthToken()) {
            url += "?auth=" + authToken;
        }
        return url;
    }
}
