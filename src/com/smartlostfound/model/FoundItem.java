package com.smartlostfound.model;

import java.time.LocalDate;

/**
 * Concrete domain model representing an item that has been found and secured.
 * Demonstrates Inheritance by extending {@link Item} and adding specialized
 * attributes such as location found, current physical storage location,
 * and finder contact notes.
 */
public class FoundItem extends Item {
    private static final long serialVersionUID = 1L;

    private String locationFound;
    private String storageLocation;
    private String finderContactInfo;

    /**
     * Constructs a new FoundItem with all attributes.
     *
     * @param id Unique identifier
     * @param name Name of the found item
     * @param category Item classification
     * @param date Date when the item was discovered
     * @param description Narrative description
     * @param status Current status (typically {@link ItemStatus#FOUND})
     * @param reporter Finder or officer logging the report
     * @param locationFound Detailed place where the item was discovered
     * @param storageLocation Safe-keeping or storage location (e.g. Office Locker #2)
     * @param finderContactInfo Additional contact information or notes
     */
    public FoundItem(String id, String name, ItemCategory category, LocalDate date,
                     String description, ItemStatus status, User reporter,
                     String locationFound, String storageLocation, String finderContactInfo) {
        super(id, name, category, date, description, status, reporter);
        setLocationFound(locationFound);
        setStorageLocation(storageLocation);
        setFinderContactInfo(finderContactInfo);
    }

    /**
     * Overloaded constructor defaulting status to {@link ItemStatus#FOUND}.
     *
     * @param id Unique identifier
     * @param name Name of the found item
     * @param category Item classification
     * @param date Date discovered
     * @param description Narrative description
     * @param reporter Person logging the item
     * @param locationFound Location discovered
     * @param storageLocation Storage location
     */
    public FoundItem(String id, String name, ItemCategory category, LocalDate date,
                     String description, User reporter, String locationFound, String storageLocation) {
        this(id, name, category, date, description, ItemStatus.FOUND, reporter,
             locationFound, storageLocation, "");
    }

    // =========================================================================
    // Polymorphic Method Implementations
    // =========================================================================

    @Override
    public String getItemType() {
        return "Found";
    }

    @Override
    public String getSpecificLocation() {
        return locationFound;
    }

    @Override
    public String getSpecificDetailsSummary() {
        if (storageLocation != null && !storageLocation.trim().isEmpty()) {
            return "Stored: " + storageLocation;
        }
        return "Storage Pending";
    }

    @Override
    public String getSpecificField1() {
        return storageLocation;
    }

    @Override
    public String getSpecificField2() {
        return finderContactInfo;
    }

    // =========================================================================
    // Encapsulated Getters & Setters
    // =========================================================================

    public String getLocationFound() {
        return locationFound;
    }

    public void setLocationFound(String locationFound) {
        this.locationFound = (locationFound == null) ? "" : locationFound.trim();
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = (storageLocation == null) ? "" : storageLocation.trim();
    }

    public String getFinderContactInfo() {
        return finderContactInfo;
    }

    public void setFinderContactInfo(String finderContactInfo) {
        this.finderContactInfo = (finderContactInfo == null) ? "" : finderContactInfo.trim();
    }
}
