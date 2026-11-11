package com.smartlostfound.model;

/**
 * Enumeration representing predefined categories for lost and found items.
 * Provides user-friendly display labels and conversion helpers.
 */
public enum ItemCategory {
    ELECTRONICS("Electronics & Gadgets"),
    DOCUMENTS_ID("Documents & Identification"),
    WALLET_PURSE("Wallets & Purses"),
    KEYS("Keys & Access Badges"),
    JEWELRY_WATCH("Jewelry & Watches"),
    BAG_BACKPACK("Bags & Backpacks"),
    CLOTHING("Clothing & Footwear"),
    BOOKS_STATIONERY("Books & Stationery"),
    OTHER("Other / Miscellaneous");

    private final String displayName;

    /**
     * Constructs an ItemCategory with its human-readable display label.
     *
     * @param displayName Display label for UI representation
     */
    ItemCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Retrieves the formatted display name.
     *
     * @return Display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Parses a string into an ItemCategory, matching by enum name or display name.
     * Defaults to {@link #OTHER} if no direct match is found.
     *
     * @param value String value to parse
     * @return Matching ItemCategory, or OTHER if null or unmatched
     */
    public static ItemCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }
        String trimmed = value.trim();
        for (ItemCategory category : values()) {
            if (category.name().equalsIgnoreCase(trimmed) || 
                category.displayName.equalsIgnoreCase(trimmed)) {
                return category;
            }
        }
        return OTHER;
    }
}
