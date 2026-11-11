package com.smartlostfound.storage;

import com.smartlostfound.model.Item;

import java.util.List;

/**
 * Interface defining the persistence operations for lost and found items.
 * Allows decoupling the storage implementation (e.g. CSV, JSON, Database)
 * from the business logic layer.
 */
public interface ItemStorageService {

    /**
     * Loads all persisted items from the storage medium.
     *
     * @return List of deserialized {@link Item} instances
     * @throws DataPersistenceException if read or parse failure occurs
     */
    List<Item> loadItems() throws DataPersistenceException;

    /**
     * Persists the provided list of items to the storage medium.
     *
     * @param items List of {@link Item} instances to store
     * @throws DataPersistenceException if write failure occurs
     */
    void saveItems(List<Item> items) throws DataPersistenceException;

    /**
     * Returns the human-readable path or identifier of the storage source.
     *
     * @return Storage destination description
     */
    String getStoragePath();

    /**
     * Checks if the underlying storage resource currently exists.
     *
     * @return True if storage file or resource exists
     */
    boolean storageExists();
}
