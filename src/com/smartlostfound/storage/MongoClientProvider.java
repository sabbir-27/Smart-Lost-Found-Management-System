package com.smartlostfound.storage;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

/**
 * Thread-safe Singleton Provider managing the MongoDB client connection pool.
 * Configures connection timeouts, server selection timeouts, and health ping checks.
 * Resilient against DNS errors, network unavailability, and invalid connection strings.
 */
public class MongoClientProvider {

    static {
        // Silence driver warnings when running without SLF4J
        java.util.logging.Logger.getLogger("org.mongodb.driver").setLevel(java.util.logging.Level.SEVERE);
    }

    private static volatile MongoClientProvider instance;

    private final MongoConfig config;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private volatile Boolean lastHealthCheck = null;

    private MongoClientProvider(MongoConfig config) {
        this.config = config != null ? config : MongoConfig.load();
        MongoClient client = null;
        MongoDatabase db = null;

        try {
            ConnectionString connString = new ConnectionString(this.config.getConnectionUri());
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(this.config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS)
                                   .readTimeout(this.config.getConnectTimeoutMs(), TimeUnit.MILLISECONDS))
                    .applyToClusterSettings(builder ->
                            builder.serverSelectionTimeout(this.config.getServerSelectionTimeoutMs(), TimeUnit.MILLISECONDS))
                    .build();

            client = MongoClients.create(settings);
            db = client.getDatabase(this.config.getDatabaseName());
        } catch (Exception e) {
            System.err.println("Notice: MongoDB client initialization failed (" + e.getMessage() + "), using local fallback mode.");
            client = null;
            db = null;
        }

        this.mongoClient = client;
        this.database = db;

        // Attach JVM shutdown hook to clean up connection pool
        if (this.mongoClient != null) {
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        }
    }

    public static MongoClientProvider getInstance() {
        if (instance == null) {
            synchronized (MongoClientProvider.class) {
                if (instance == null) {
                    instance = new MongoClientProvider(MongoConfig.load());
                }
            }
        }
        return instance;
    }

    public static MongoClientProvider getInstance(MongoConfig config) {
        if (instance == null) {
            synchronized (MongoClientProvider.class) {
                if (instance == null) {
                    instance = new MongoClientProvider(config);
                }
            }
        }
        return instance;
    }

    /**
     * Executes a ping command to verify database connectivity.
     *
     * @return True if MongoDB server responds within timeout
     */
    public synchronized boolean isAvailable() {
        if (this.database == null || this.mongoClient == null) {
            this.lastHealthCheck = false;
            return false;
        }
        try {
            Document pingResult = database.runCommand(new Document("ping", 1));
            boolean ok = pingResult != null && (pingResult.get("ok") instanceof Number && ((Number) pingResult.get("ok")).intValue() == 1);
            this.lastHealthCheck = ok;
            return ok;
        } catch (Exception e) {
            this.lastHealthCheck = false;
            return false;
        }
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public MongoConfig getConfig() {
        return config;
    }

    public synchronized void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception ignored) {}
        }
    }
}
