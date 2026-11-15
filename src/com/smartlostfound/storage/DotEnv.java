package com.smartlostfound.storage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Lightweight .env file loader for application configuration.
 * Searches for .env in the working directory or project root,
 * parsing KEY=VALUE pairs while supporting comments (#) and quotes.
 */
public class DotEnv {

    private static final String DEFAULT_ENV_FILE = ".env";
    private static final Map<String, String> ENV_VARS = new HashMap<>();
    private static boolean loaded = false;

    static {
        load();
    }

    public static synchronized void load() {
        if (loaded) return;
        Path envPath = Paths.get(System.getProperty("env.file", DEFAULT_ENV_FILE));
        if (!Files.exists(envPath)) {
            // Also check data/.env if root .env not found
            Path dataEnv = Paths.get("data", ".env");
            if (Files.exists(dataEnv)) {
                envPath = dataEnv;
            }
        }

        if (Files.exists(envPath)) {
            try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int equalsIdx = line.indexOf('=');
                    if (equalsIdx > 0) {
                        String key = line.substring(0, equalsIdx).trim();
                        String value = line.substring(equalsIdx + 1).trim();
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }
                        ENV_VARS.put(key, value);
                    }
                }
            } catch (IOException e) {
                System.err.println("Notice: Could not read .env file: " + e.getMessage());
            }
        }
        loaded = true;
    }

    public static synchronized void reload(Path customPath) {
        ENV_VARS.clear();
        loaded = false;
        if (customPath != null) {
            System.setProperty("env.file", customPath.toString());
        }
        load();
    }

    /**
     * Resolves configuration key with priority:
     * 1. System environment variable
     * 2. Java system property
     * 3. .env file
     * 4. Default fallback value
     */
    public static String get(String key, String defaultValue) {
        String val = System.getenv(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        val = System.getProperty(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        val = System.getProperty(key.toLowerCase().replace('_', '.'));
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        val = ENV_VARS.get(key);
        if (val != null && !val.trim().isEmpty()) {
            return val.trim();
        }

        return defaultValue;
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null || val.trim().isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean containsKey(String key) {
        return ENV_VARS.containsKey(key);
    }

    public static Map<String, String> getAll() {
        return Collections.unmodifiableMap(ENV_VARS);
    }
}
