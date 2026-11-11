package com.smartlostfound.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Abstract base class representing an item registered in the Smart Lost &amp; Found system.
 * Demonstrates Abstraction and Encapsulation by shielding common item attributes
 * and defining polymorphic contracts for subclass-specific behaviors.
 */
public abstract class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String id;
    private String name;
    private ItemCategory category;
    private LocalDate date;
    private String description;
    private ItemStatus status;
    private User reporter;
    private String resolutionNotes;
    private LocalDateTime createdAt;

    /**
     * Protected constructor to be invoked by subclass constructors.
     *
     * @param id Unique identifier of the item
     * @param name Descriptive title or name of the item
     * @param category Classification category
     * @param date Date when item was lost or found
     * @param description Detailed narrative description
     * @param status Current lifecycle status
     * @param reporter Person who logged the report
     */
    protected Item(String id, String name, ItemCategory category, LocalDate date,
                   String description, ItemStatus status, User reporter) {
        setId(id);
        setName(name);
        setCategory(category);
        setDate(date);
        setDescription(description);
        setStatus(status);
        setReporter(reporter);
        this.resolutionNotes = "";
        this.createdAt = LocalDateTime.now();
    }

    // =========================================================================
    // Abstract Methods (Polymorphism & Abstraction)
    // =========================================================================

    /**
     * Returns the human-readable item type ("Lost" or "Found").
     *
     * @return Item type classification
     */
    public abstract String getItemType();

    /**
     * Returns the specific location pertinent to this item type
     * (e.g., location lost or location found).
     *
     * @return Location description string
     */
    public abstract String getSpecificLocation();

    /**
     * Returns a summarized string of subclass-specific attributes
     * (e.g., reward amount for lost items, storage location for found items).
     *
     * @return Formatted summary string
     */
    public abstract String getSpecificDetailsSummary();

    /**
     * Returns the first subclass-specific field for persistence serialization.
     *
     * @return Subclass specific field 1 (e.g., reward amount or storage location)
     */
    public abstract String getSpecificField1();

    /**
     * Returns the second subclass-specific field for persistence serialization.
     *
     * @return Subclass specific field 2 (e.g., empty or finder additional contact)
     */
    public abstract String getSpecificField2();

    // =========================================================================
    // Encapsulated Getters & Setters
    // =========================================================================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Item ID cannot be null or empty.");
        }
        this.id = id.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty.");
        }
        this.name = name.trim();
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = (category != null) ? category : ItemCategory.OTHER;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = (date != null) ? date : LocalDate.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = (description == null) ? "" : description.trim();
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Item status cannot be null.");
        }
        this.status = status;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = (reporter != null) ? reporter : new User();
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = (resolutionNotes == null) ? "" : resolutionNotes.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    /**
     * Returns formatted date string (yyyy-MM-dd).
     *
     * @return Formatted date
     */
    public String getFormattedDate() {
        return (date != null) ? date.format(DATE_FORMATTER) : "";
    }

    /**
     * Returns formatted timestamp when report was logged.
     *
     * @return Formatted datetime
     */
    public String getFormattedCreatedAt() {
        return (createdAt != null) ? createdAt.format(DATETIME_FORMATTER) : "";
    }

    /**
     * Checks if this item has been resolved (recovered or claimed).
     *
     * @return True if item is resolved
     */
    public boolean isResolved() {
        return status != null && status.isResolved();
    }

    /**
     * Performs a comprehensive case-insensitive keyword match across all item attributes.
     *
     * @param keyword Search term to match
     * @return True if keyword matches any attribute
     */
    public boolean matchesKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String term = keyword.trim().toLowerCase();
        return (id != null && id.toLowerCase().contains(term)) ||
               (name != null && name.toLowerCase().contains(term)) ||
               (category != null && category.getDisplayName().toLowerCase().contains(term)) ||
               (description != null && description.toLowerCase().contains(term)) ||
               (getSpecificLocation() != null && getSpecificLocation().toLowerCase().contains(term)) ||
               (getSpecificDetailsSummary() != null && getSpecificDetailsSummary().toLowerCase().contains(term)) ||
               (status != null && status.getDisplayName().toLowerCase().contains(term)) ||
               (reporter != null && (reporter.getName().toLowerCase().contains(term) ||
                                     reporter.getEmail().toLowerCase().contains(term) ||
                                     reporter.getPhone().toLowerCase().contains(term))) ||
               (resolutionNotes != null && resolutionNotes.toLowerCase().contains(term));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(id, item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s (%s) - %s",
                getItemType(), id, name, category.getDisplayName(), status.getDisplayName());
    }
}
