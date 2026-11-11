package com.smartlostfound.model;

/**
 * Access control roles within the Smart Lost &amp; Found System.
 * Distinguishes between standard community users and system administrators.
 */
public enum UserRole {
    USER("Standard User", "Report, track personal claims, and browse community feed"),
    ADMIN("Administrator", "Full system oversight, moderation, inventory, and user management");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
