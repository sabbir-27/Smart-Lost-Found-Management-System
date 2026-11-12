package com.smartlostfound.service;

import com.smartlostfound.model.UserAccount;
import com.smartlostfound.model.UserRole;
import com.smartlostfound.storage.CsvUserStorageService;
import com.smartlostfound.storage.DataPersistenceException;
import com.smartlostfound.storage.FirebaseUserStorageService;
import com.smartlostfound.storage.UserStorageService;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service managing user authentication, registration, access control,
 * and persistent storage of user credentials.
 */
public class UserManager {
    private static final String DEFAULT_USER_STORAGE_PATH = "data/users.csv";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]{3,24}$");

    private final Map<String, UserAccount> usersByUsername = new ConcurrentHashMap<>();
    private final UserStorageService storageService;

    /**
     * Default constructor connecting to Firebase Cloud Storage with local CSV fallback.
     */
    public UserManager() {
        this(new FirebaseUserStorageService());
    }

    public UserManager(UserStorageService storageService) {
        this.storageService = Objects.requireNonNull(storageService, "Storage service cannot be null");
        initializeData();
    }

    /**
     * Loads persisted accounts or seeds default credentials.
     */
    public synchronized void initializeData() {
        try {
            if (storageService.storageExists()) {
                List<UserAccount> loaded = storageService.loadUsers();
                usersByUsername.clear();
                if (!loaded.isEmpty()) {
                    for (UserAccount user : loaded) {
                        usersByUsername.put(user.getUsername().toLowerCase(), user);
                    }
                } else {
                    seedDefaultAccounts();
                    persistCurrentState();
                }
            } else {
                seedDefaultAccounts();
                persistCurrentState();
            }
        } catch (DataPersistenceException e) {
            System.err.println("Failed to load users from storage, falling back to default seed: " + e.getMessage());
            seedDefaultAccounts();
        }
    }

    /**
     * Seeds initial Administrator and standard User demo accounts.
     */
    private void seedDefaultAccounts() {
        usersByUsername.clear();

        // 1. Default System Administrator
        UserAccount admin = UserAccount.create(
                "admin",
                "admin123",
                UserRole.ADMIN,
                "System Administrator",
                "admin@lostfound.org",
                "+1-800-ADMIN-01"
        );
        usersByUsername.put(admin.getUsername().toLowerCase(), admin);

        // 2. Demo User 1: John Doe
        UserAccount john = UserAccount.create(
                "john",
                "user123",
                UserRole.USER,
                "John Doe",
                "john.doe@example.com",
                "+1-555-0199"
        );
        usersByUsername.put(john.getUsername().toLowerCase(), john);

        // 3. Demo User 2: Sarah Jenkins
        UserAccount sarah = UserAccount.create(
                "sarah",
                "user123",
                UserRole.USER,
                "Sarah Jenkins",
                "sarah.j@example.com",
                "+1-555-0142"
        );
        usersByUsername.put(sarah.getUsername().toLowerCase(), sarah);
    }

    /**
     * Persists all currently registered users to disk.
     */
    public synchronized void persistCurrentState() throws DataPersistenceException {
        storageService.saveUsers(new ArrayList<>(usersByUsername.values()));
    }

    /**
     * Authenticates a user by username and password.
     *
     * @param username Candidate username (case-insensitive)
     * @param password Candidate plain password
     * @return Authenticated {@link UserAccount}
     * @throws IllegalArgumentException if credentials invalid or account suspended
     */
    public UserAccount authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        UserAccount user = usersByUsername.get(username.trim().toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Account not found for username: " + username.trim());
        }

        if (!user.isActive()) {
            throw new IllegalArgumentException("This account is currently deactivated. Contact a System Administrator.");
        }

        if (!user.verifyPassword(password)) {
            throw new IllegalArgumentException("Invalid password. Please try again.");
        }

        return user;
    }

    /**
     * Registers a new user account with validation and disk persistence.
     */
    public synchronized UserAccount registerUser(String username, String password, String name,
                                                 String email, String phone, UserRole role)
            throws DataPersistenceException {
        // 1. Validate username
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        String cleanUsername = username.trim().toLowerCase();
        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw new IllegalArgumentException("Username must be 3-24 characters (letters, numbers, underscore, hyphen, dot).");
        }
        if (usersByUsername.containsKey(cleanUsername)) {
            throw new IllegalArgumentException("Username '" + cleanUsername + "' is already registered. Please choose another.");
        }

        // 2. Validate password
        if (password == null || password.length() < 4) {
            throw new IllegalArgumentException("Password must be at least 4 characters.");
        }

        // 3. Validate name and contact
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        String cleanName = name.trim();
        String cleanEmail = (email == null) ? "" : email.trim();
        String cleanPhone = (phone == null) ? "" : phone.trim();

        if (cleanEmail.isEmpty() && cleanPhone.isEmpty()) {
            throw new IllegalArgumentException("Please provide at least an email address or phone number.");
        }

        UserRole cleanRole = (role == null) ? UserRole.USER : role;

        UserAccount newUser = UserAccount.create(cleanUsername, password, cleanRole, cleanName, cleanEmail, cleanPhone);
        usersByUsername.put(cleanUsername, newUser);
        persistCurrentState();

        return newUser;
    }

    /**
     * Authenticates an existing user matching the Google email, or creates and registers
     * a new Standard User account with the Google profile credentials.
     *
     * @param googleEmail Google email address (e.g. alex.rivera@gmail.com)
     * @param fullName Full name of the user (e.g. Alex Rivera)
     * @return Authenticated UserAccount
     * @throws DataPersistenceException if disk write fails
     */
    public synchronized UserAccount authenticateOrRegisterGoogle(String googleEmail, String fullName)
            throws DataPersistenceException {
        if (googleEmail == null || googleEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Google email cannot be empty.");
        }
        String cleanEmail = googleEmail.trim().toLowerCase();

        // 1. Check if user already exists by email (case-insensitive)
        for (UserAccount existing : usersByUsername.values()) {
            if (cleanEmail.equalsIgnoreCase(existing.getEmail())) {
                if (!existing.isActive()) {
                    throw new IllegalArgumentException("This Google account is currently deactivated. Contact an administrator.");
                }
                return existing;
            }
        }

        // 2. Generate a clean, unique username from the email prefix
        String rawPrefix = cleanEmail.contains("@") ? cleanEmail.split("@")[0] : cleanEmail;
        String baseUsername = rawPrefix.replaceAll("[^a-zA-Z0-9_.-]", "");
        if (baseUsername.length() < 3) {
            baseUsername = "user_" + baseUsername;
        }
        if (baseUsername.length() > 20) {
            baseUsername = baseUsername.substring(0, 20);
        }

        String candidateUsername = baseUsername;
        int counter = 1;
        while (usersByUsername.containsKey(candidateUsername.toLowerCase())) {
            candidateUsername = baseUsername + counter;
            counter++;
        }

        String cleanName = (fullName != null && !fullName.trim().isEmpty()) ? fullName.trim() : rawPrefix;
        String generatedPass = "GOOGLE_OAUTH_" + UUID.randomUUID().toString();

        UserAccount newGoogleAccount = UserAccount.create(
                candidateUsername,
                generatedPass,
                UserRole.USER,
                cleanName,
                cleanEmail,
                ""
        );

        usersByUsername.put(candidateUsername.toLowerCase(), newGoogleAccount);
        persistCurrentState();
        return newGoogleAccount;
    }

    /**
     * Retrieves an account by username.
     */
    public Optional<UserAccount> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByUsername.get(username.trim().toLowerCase()));
    }

    /**
     * Returns an unmodifiable list of all registered users sorted by role and username.
     */
    public List<UserAccount> getAllUsers() {
        return usersByUsername.values().stream()
                .sorted(Comparator.comparing(UserAccount::isAdmin).reversed()
                        .thenComparing(UserAccount::getUsername))
                .collect(Collectors.toList());
    }

    /**
     * Updates an account's role (Admin / User).
     */
    public synchronized void updateUserRole(String username, UserRole newRole) throws DataPersistenceException {
        UserAccount account = usersByUsername.get(username.toLowerCase());
        if (account == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        if (account.isAdmin() && newRole != UserRole.ADMIN && getAdminCount() <= 1) {
            throw new IllegalStateException("Cannot demote the last remaining Administrator in the system.");
        }
        account.setRole(newRole);
        persistCurrentState();
    }

    /**
     * Toggles an account active status.
     */
    public synchronized void toggleUserStatus(String username) throws DataPersistenceException {
        UserAccount account = usersByUsername.get(username.toLowerCase());
        if (account == null) {
            throw new IllegalArgumentException("User not found: " + username);
        }
        if (account.isAdmin() && account.isActive() && getAdminCount() <= 1) {
            throw new IllegalStateException("Cannot deactivate the only active Administrator in the system.");
        }
        account.setActive(!account.isActive());
        persistCurrentState();
    }

    /**
     * Permanently deletes a user account.
     */
    public synchronized boolean deleteUser(String username, String requestingUsername) throws DataPersistenceException {
        String clean = username.toLowerCase();
        if (clean.equalsIgnoreCase(requestingUsername)) {
            throw new IllegalArgumentException("You cannot delete your own logged-in account.");
        }
        UserAccount account = usersByUsername.get(clean);
        if (account == null) {
            return false;
        }
        if (account.isAdmin() && getAdminCount() <= 1) {
            throw new IllegalStateException("Cannot delete the last remaining Administrator in the system.");
        }
        usersByUsername.remove(clean);
        persistCurrentState();
        return true;
    }

    public int getTotalCount() {
        return usersByUsername.size();
    }

    public int getAdminCount() {
        return (int) usersByUsername.values().stream().filter(UserAccount::isAdmin).count();
    }

    public int getActiveCount() {
        return (int) usersByUsername.values().stream().filter(UserAccount::isActive).count();
    }

    public String getStoragePath() {
        return storageService.getStoragePath();
    }
}
