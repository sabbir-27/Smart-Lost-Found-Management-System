package com.smartlostfound.model;

import java.time.LocalDate;

/**
 * Model representing an item reported as lost.
 * Includes loss location and optional reward amount.
 */
public class LostItem extends Item {
    private static final long serialVersionUID = 1L;

    private String locationLost;
    private double rewardAmount;

    /**
     * Constructs a new LostItem with all required and specific details.
     *
     * @param id Unique identifier
     * @param name Name of the lost item
     * @param category Item classification
     * @param date Date when the item was lost
     * @param description Narrative description
     * @param status Current status (typically {@link ItemStatus#LOST})
     * @param reporter Person who lost the item
     * @param locationLost Detailed location where the item was lost
     * @param rewardAmount Monetary reward offered (0.0 if none)
     */
    public LostItem(String id, String name, ItemCategory category, LocalDate date,
                    String description, ItemStatus status, User reporter,
                    String locationLost, double rewardAmount) {
        super(id, name, category, date, description, status, reporter);
        setLocationLost(locationLost);
        setRewardAmount(rewardAmount);
    }

    /**
     * Overloaded constructor defaulting status to {@link ItemStatus#LOST} and reward to 0.0.
     *
     * @param id Unique identifier
     * @param name Name of the lost item
     * @param category Item classification
     * @param date Date lost
     * @param description Narrative description
     * @param reporter Person who lost the item
     * @param locationLost Location where lost
     */
    public LostItem(String id, String name, ItemCategory category, LocalDate date,
                    String description, User reporter, String locationLost) {
        this(id, name, category, date, description, ItemStatus.LOST, reporter, locationLost, 0.0);
    }

    // =========================================================================
    // Polymorphic Method Implementations
    // =========================================================================

    @Override
    public String getItemType() {
        return "Lost";
    }

    @Override
    public String getSpecificLocation() {
        return locationLost;
    }

    @Override
    public String getSpecificDetailsSummary() {
        if (rewardAmount > 0.0) {
            return String.format("Reward: $%.2f", rewardAmount);
        }
        return "No Reward";
    }

    @Override
    public String getSpecificField1() {
        return String.valueOf(rewardAmount);
    }

    @Override
    public String getSpecificField2() {
        return "";
    }

    // =========================================================================
    // Encapsulated Getters & Setters
    // =========================================================================

    public String getLocationLost() {
        return locationLost;
    }

    public void setLocationLost(String locationLost) {
        this.locationLost = (locationLost == null) ? "" : locationLost.trim();
    }

    public double getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(double rewardAmount) {
        if (rewardAmount < 0.0) {
            throw new IllegalArgumentException("Reward amount cannot be negative.");
        }
        this.rewardAmount = rewardAmount;
    }

    /**
     * Returns a formatted currency string for display.
     *
     * @return Formatted reward or "None"
     */
    public String getFormattedReward() {
        return rewardAmount > 0.0 ? String.format("$%.2f", rewardAmount) : "None";
    }
}
