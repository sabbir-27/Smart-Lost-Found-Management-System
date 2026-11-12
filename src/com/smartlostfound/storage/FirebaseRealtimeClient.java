package com.smartlostfound.storage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Lightweight, zero-dependency REST Client for Firebase Realtime Database.
 * Utilizes Java 11+ native HttpClient to perform synchronous/asynchronous cloud persistence.
 */
public class FirebaseRealtimeClient {

    private final FirebaseConfig config;
    private final HttpClient httpClient;
    private int lastStatusCode = 0;
    private String lastErrorMessage = null;

    public FirebaseRealtimeClient(FirebaseConfig config) {
        this.config = config != null ? config : FirebaseConfig.load();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /**
     * Performs a GET request on the specified resource path.
     *
     * @param path Resource sub-path (e.g. "items" or "users")
     * @return Raw JSON response string or null
     * @throws IOException if network transmission fails
     */
    public String get(String path) throws IOException, InterruptedException {
        String url = config.buildEndpoint(path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        this.lastStatusCode = response.statusCode();

        if (response.statusCode() == 200) {
            String body = response.body();
            if ("null".equals(body) || body == null) {
                return null;
            }
            return body;
        } else if (response.statusCode() == 401) {
            this.lastErrorMessage = "Firebase Permission Denied (401). Update Realtime Database security rules.";
            throw new IOException(lastErrorMessage);
        } else if (response.statusCode() == 404) {
            return null;
        } else {
            this.lastErrorMessage = "Firebase HTTP Error: " + response.statusCode() + " - " + response.body();
            throw new IOException(lastErrorMessage);
        }
    }

    /**
     * Performs a PUT request to replace or set data at the specified path.
     *
     * @param path Resource sub-path (e.g. "items")
     * @param jsonPayload Valid JSON payload
     * @return Response string from Firebase
     * @throws IOException if write fails
     */
    public String put(String path, String jsonPayload) throws IOException, InterruptedException {
        String url = config.buildEndpoint(path);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload != null ? jsonPayload : "{}"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        this.lastStatusCode = response.statusCode();

        if (response.statusCode() == 200) {
            return response.body();
        } else if (response.statusCode() == 401) {
            this.lastErrorMessage = "Firebase Permission Denied (401). Update Realtime Database security rules.";
            throw new IOException(lastErrorMessage);
        } else {
            this.lastErrorMessage = "Firebase PUT Error " + response.statusCode() + ": " + response.body();
            throw new IOException(lastErrorMessage);
        }
    }

    /**
     * Tests connectivity to the configured Firebase instance.
     *
     * @return True if read permissions are granted
     */
    public boolean testConnection() {
        try {
            String url = config.buildEndpoint(".settings/rules");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.buildEndpoint("test_ping")))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            this.lastStatusCode = response.statusCode();
            return response.statusCode() == 200 || response.statusCode() == 204;
        } catch (Exception e) {
            this.lastErrorMessage = e.getMessage();
            return false;
        }
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public FirebaseConfig getConfig() {
        return config;
    }
}
