package com.smartlostfound.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an authenticated user or administrator account in the system.
 * Extends {@link User} to adhere to OOP principles (Inheritance &amp; Polymorphism),
 * allowing any UserAccount to be passed wherever a User reporter is required.
 */
public class UserAccount extends User {
    private static final long serialVersionUID = 1L;

    private String username;
    private String passwordHash;
    private UserRole role;
    private LocalDateTime createdAt;
    private boolean active;

    /**
     * Default constructor for serialization and reflection.
     */
    public UserAccount() {
        super();
        this.username = "";
        this.passwordHash = "";
        this.role = UserRole.USER;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    /**
     * Constructs a UserAccount with full credentials and profile details.
     *
     * @param username Unique identifier for system authentication
     * @param passwordHash Hex-encoded SHA-256 hash of the password
     * @param role Access control role (USER or ADMIN)
     * @param name Full legal or display name
     * @param email Contact email address
     * @param phone Contact phone number
     * @param createdAt Account creation timestamp
     * @param active Account status flag
     */
    public UserAccount(String username, String passwordHash, UserRole role,
                       String name, String email, String phone,
                       LocalDateTime createdAt, boolean active) {
        super(name, email, phone);
        setUsername(username);
        this.passwordHash = (passwordHash == null) ? "" : passwordHash;
        this.role = (role == null) ? UserRole.USER : role;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
        this.active = active;
    }

    /**
     * Factory method creating a new active user account from a plain-text password.
     *
     * @param username Unique username
     * @param plainPassword Plain-text password to hash
     * @param role Access control role
     * @param name Full display name
     * @param email Contact email
     * @param phone Contact phone number
     * @return Newly initialized {@link UserAccount} instance
     */
    public static UserAccount create(String username, String plainPassword, UserRole role,
                                     String name, String email, String phone) {
        return new UserAccount(
                username,
                hashPassword(plainPassword),
                role,
                name,
                email,
                phone,
                LocalDateTime.now(),
                true
        );
    }

    /**
     * Computes the SHA-256 hexadecimal hash for a plain-text password.
     *
     * @param plainPassword Raw password string
     * @return Hexadecimal representation of SHA-256 digest
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            plainPassword = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 hashing algorithm not available in JVM", e);
        }
    }

    /**
     * Verifies whether a candidate plain-text password matches this account's hash.
     *
     * @param candidatePassword Raw password entered during login
     * @return True if candidate password matches stored hash
     */
    public boolean verifyPassword(String candidatePassword) {
        if (candidatePassword == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword);
        return candidateHash.equalsIgnoreCase(this.passwordHash);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = (username == null) ? "" : username.trim().toLowerCase();
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = (passwordHash == null) ? "" : passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = (role == null) ? UserRole.USER : role;
    }

    public boolean isAdmin() {
        return role != null && role.isAdmin();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns initials of the account owner for avatar rendering (e.g. "JD" for John Doe).
     *
     * @return 1 or 2 letter uppercase initials
     */
    public String getInitials() {
        String n = getName();
        if (n == null || n.trim().isEmpty()) {
            n = getUsername();
        }
        if (n == null || n.trim().isEmpty()) {
            return "U";
        }
        String[] parts = n.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), username);
    }

    @Override
    public String toString() {
        return String.format("%s (@%s) [%s]", getName(), username, role);
    }
}
