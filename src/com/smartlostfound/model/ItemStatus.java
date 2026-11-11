package com.smartlostfound.model;

import java.awt.Color;

/**
 * Enumeration representing the current lifecycle status of an item in the system.
 * Includes color definitions for UI badge rendering.
 */
public enum ItemStatus {
    LOST("Lost", new Color(185, 28, 28), new Color(254, 226, 226)),
    FOUND("Found", new Color(29, 78, 216), new Color(219, 234, 254)),
    RECOVERED("Recovered", new Color(21, 128, 61), new Color(220, 252, 231)),
    CLAIMED("Claimed", new Color(15, 118, 110), new Color(204, 251, 241)),
    ARCHIVED("Archived", new Color(71, 85, 105), new Color(241, 245, 249));

    private final String displayName;
    private final Color foregroundColor;
    private final Color backgroundColor;

    /**
     * Constructs an ItemStatus with display name and badge styling colors.
     *
     * @param displayName Human-readable status name
     * @param foregroundColor Text color for UI rendering
     * @param backgroundColor Badge background color for UI rendering
     */
    ItemStatus(String displayName, Color foregroundColor, Color backgroundColor) {
        this.displayName = displayName;
        this.foregroundColor = foregroundColor;
        this.backgroundColor = backgroundColor;
    }

    /**
     * Retrieves the formatted display name.
     *
     * @return Status display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retrieves the foreground text color for badge rendering.
     *
     * @return Foreground Color
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Retrieves the background color for badge rendering.
     *
     * @return Background Color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Indicates whether this status represents a resolved state.
     *
     * @return True if RECOVERED, CLAIMED, or ARCHIVED
     */
    public boolean isResolved() {
        return this == RECOVERED || this == CLAIMED || this == ARCHIVED;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parses a string into an ItemStatus, matching by name or display name.
     *
     * @param value Textual representation of status
     * @return Matching ItemStatus, or LOST by default
     */
    public static ItemStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LOST;
        }
        String trimmed = value.trim();
        for (ItemStatus status : values()) {
            if (status.name().equalsIgnoreCase(trimmed) || 
                status.displayName.equalsIgnoreCase(trimmed)) {
                return status;
            }
        }
        return LOST;
    }
}
