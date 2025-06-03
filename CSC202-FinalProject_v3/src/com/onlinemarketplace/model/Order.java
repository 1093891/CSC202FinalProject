package com.onlinemarketplace.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; // Using List interface
import java.util.Objects;

/**
 * Represents a customer's order in the Online Marketplace System.
 * Stores details about the order, including the customer, products, quantities, date, and status.
 */
public class Order {
    private String orderId;
    private String customerId;
    private LocalDate orderDate;
    private List<OrderItem> orderedItems; // Changed from Map<String, Integer> to List<OrderItem>
    private String status; // e.g., "Pending", "Confirmed", "Cancelled", "Delivered"
    private double totalAmount;

    /**
     * Constructs a new Order instance.
     *
     * @param orderId The unique identifier for the order.
     * @param customerId The ID of the customer who placed the order.
     * @param orderDate The date the order was placed.
     * @param orderedItems A list of OrderItem objects representing products and their quantities.
     * @param totalAmount The total monetary amount of the order.
     * @param status The current status of the order.
     */
    public Order(String orderId, String customerId, LocalDate orderDate, List<OrderItem> orderedItems, double totalAmount, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.orderedItems = new ArrayList<>(orderedItems); // Defensive copy
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // --- Getters ---
    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public List<OrderItem> getOrderedItems() {
        return new ArrayList<>(orderedItems); // Return a defensive copy
    }

    public String getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    // --- Setters ---
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public void setOrderedItems(List<OrderItem> orderedItems) {
        this.orderedItems = new ArrayList<>(orderedItems);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return String.format("Order ID: %s, Customer ID: %s, Date: %s, Total: $%.2f, Status: %s",
                orderId, customerId, orderDate, totalAmount, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}