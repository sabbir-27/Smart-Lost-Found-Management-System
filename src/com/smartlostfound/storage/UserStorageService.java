package com.smartlostfound.storage;

import com.smartlostfound.model.UserAccount;

import java.util.List;

/**
 * Persistence service interface for user and administrator accounts.
 * Enables decoupling storage formats (CSV, Database, etc.) from authentication logic.
 */
public interface UserStorageService {

    /**
     * Loads all persisted user accounts from storage.
     *
     * @return List of persisted {@link UserAccount} objects
     * @throws DataPersistenceException if read or parse failure occurs
     */
    List<UserAccount> loadUsers() throws DataPersistenceException;

    /**
     * Persists the given list of user accounts to storage.
     *
     * @param users List of {@link UserAccount} objects to store
     * @throws DataPersistenceException if write failure occurs
     */
    void saveUsers(List<UserAccount> users) throws DataPersistenceException;

    /**
     * Returns the human-readable path or identifier of the storage source.
     *
     * @return Storage destination description
     */
    String getStoragePath();

    /**
     * Checks if the underlying user storage file or table currently exists.
     *
     * @return True if storage resource exists
     */
    boolean storageExists();
}
