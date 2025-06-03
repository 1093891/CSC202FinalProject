
package com.onlinemarketplace.model;

import com.onlinemarketplace.exception.HarvestDateException;
import com.onlinemarketplace.exception.ValidationException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a farm product available in the marketplace.
 * This class encapsulates product details, including validation for price and harvest date.
 */
public class Product {
    private String productId;
    private String name;
    private String description; // e.g., organic, seasonal, category (vegetable, fruit, dairy)
    private double price;
    private int quantityAvailable;
    private LocalDate harvestDate;
    private String farmerId; // To link product to a specific farmer

    /**
     * Constructs a new Product instance.
     *
     * @param productId The unique identifier for the product.
     * @param name The name of the product.
     * @param description A description of the product (e.g., organic, seasonal, category).
     * @param price The price of the product.
     * @param quantityAvailable The quantity of the product currently available.
     * @param harvestDate The date when the product was harvested.
     * @param farmerId The ID of the farmer who owns this product.
     * @throws ValidationException If the price is not positive.
     * @throws HarvestDateException If the harvest date is in the future.
     */
    public Product(String productId, String name, String description, double price, int quantityAvailable, LocalDate harvestDate, String farmerId)
            throws ValidationException, HarvestDateException {
        this.productId = productId;
        this.name = name;
        this.description = description;
        setPrice(price); // Use setter for validation
        setQuantityAvailable(quantityAvailable); // Use setter for validation
        setHarvestDate(harvestDate); // Use setter for validation
        this.farmerId = farmerId;
    }

    // --- Getters ---

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }

    public LocalDate getHarvestDate() {
        return harvestDate;
    }

    public String getFarmerId() {
        return farmerId;
    }

    // --- Setters with Validation ---

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the price of the product.
     *
     * @param price The new price.
     * @throws ValidationException If the price is not positive.
     */
    public void setPrice(double price) throws ValidationException {
        if (price <= 0) {
            throw new ValidationException("Price must be a positive value.");
        }
        this.price = price;
    }

    /**
     * Sets the available quantity of the product.
     *
     * @param quantityAvailable The new quantity.
     * @throws ValidationException If the quantity is negative.
     */
    public void setQuantityAvailable(int quantityAvailable) throws ValidationException {
        if (quantityAvailable < 0) {
            throw new ValidationException("Quantity available cannot be negative.");
        }
        this.quantityAvailable = quantityAvailable;
    }

    /**
     * Sets the harvest date of the product.
     *
     * @param harvestDate The new harvest date.
     * @throws HarvestDateException If the harvest date is in the future.
     */
    public void setHarvestDate(LocalDate harvestDate) throws HarvestDateException {
        if (harvestDate == null) {
            throw new HarvestDateException("Harvest date cannot be null.");
        }
        // Harvest dates should not be future-dated for "harvested" products.
        // The requirement "Ensure harvest dates are future-dated" seems contradictory for a 'harvest date'.
        // Interpreting it as: the harvest date must be today or in the past,
        // unless it refers to *expected* harvest dates for pre-orders.
        // For simplicity, let's assume harvest date must be today or in the past.
        if (harvestDate.isAfter(LocalDate.now())) {
            throw new HarvestDateException("Harvest date cannot be in the future.");
        }
        this.harvestDate = harvestDate;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    /**
     * Checks if the product is currently in season based on its harvest date.
     * This is a simplified check; a real system would use more complex seasonality logic.
     * For now, products harvested within the last 30 days are considered "in season".
     *
     * @param currentDate The current date to check against.
     * @return true if the product is considered in season, false otherwise.
     */
    public boolean isInSeason(LocalDate currentDate) {
        if (harvestDate == null) {
            return false;
        }
        // Example: In season if harvested within the last 30 days
        return !harvestDate.isBefore(currentDate.minusDays(30)) && !harvestDate.isAfter(currentDate);
    }

    @Override
    public String toString() {
        return String.format("%s - %s ($%.2f) - Qty: %d - Harvest: %s",
                name, description, price, quantityAvailable, harvestDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
