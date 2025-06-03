package com.onlinemarketplace.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a customer's subscription to a farmer in the Online Marketplace System.
 */
public class Subscription {
    private String subscriptionId;
    private String customerId;
    private String farmerId;
    private LocalDate startDate;
    private String status; // e.g., "Active", "Cancelled", "Paused"
    private String subscriptionType; // e.g., "Weekly Vegetable Box", "Monthly Dairy"

    /**
     * Constructs a new Subscription instance.
     *
     * @param subscriptionId The unique identifier for the subscription.
     * @param customerId The ID of the subscribing customer.
     * @param farmerId The ID of the farmer being subscribed to.
     * @param startDate The date the subscription started.
     * @param status The current status of the subscription.
     * @param subscriptionType The type of subscription.
     */
    public Subscription(String subscriptionId, String customerId, String farmerId, LocalDate startDate, String status, String subscriptionType) {
        this.subscriptionId = subscriptionId;
        this.customerId = customerId;
        this.farmerId = farmerId;
        this.startDate = startDate;
        this.status = status;
        this.subscriptionType = subscriptionType;
    }

    // --- Getters ---
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getStatus() {
        return status;
    }

    public String getSubscriptionType() {
        return subscriptionType;
    }

    // --- Setters ---
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubscriptionType(String subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    @Override
    public String toString() {
        return String.format("Subscription ID: %s, Customer: %s, Farmer: %s, Type: %s, Status: %s",
                subscriptionId, customerId, farmerId, subscriptionType, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return Objects.equals(subscriptionId, that.subscriptionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriptionId);
    }
}