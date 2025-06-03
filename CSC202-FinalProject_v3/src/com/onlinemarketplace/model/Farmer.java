package com.onlinemarketplace.model;

import com.onlinemarketplace.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a farmer in the Online Marketplace System.
 * Inherits from the abstract User class and includes farmer-specific details
 * like farm address and a list of products they offer.
 */
public class Farmer extends User {
    private String farmAddress;
    private List<Product> availableProducts; // Products offered by this farmer

    /**
     * Constructs a new Farmer instance.
     *
     * @param farmerId The unique identifier for the farmer.
     * @param name The name of the farmer.
     * @param email The email address of the farmer.
     * @param password The password for farmer authentication.
     * @param farmAddress The address of the farmer's farm.
     * @throws ValidationException If email or farm address is invalid.
     */
    public Farmer(String farmerId, String name, String email, String password, String farmAddress)
            throws ValidationException {
        super(farmerId, name, email, password, "Farmer"); // Set user type
        setFarmAddress(farmAddress); // Use setter for validation
        this.availableProducts = new ArrayList<>();
    }

    // --- Getters ---

    public String getFarmAddress() {
        return farmAddress;
    }

    public List<Product> getAvailableProducts() {
        return availableProducts;
    }

    // --- Setters with Validation ---

    /**
     * Sets the farm address for the farmer.
     *
     * @param farmAddress The new farm address.
     * @throws ValidationException If the farm address is null or empty.
     */
    public void setFarmAddress(String farmAddress) throws ValidationException {
        if (farmAddress == null || farmAddress.trim().isEmpty()) {
            throw new ValidationException("Farm address cannot be empty.");
        }
        this.farmAddress = farmAddress;
    }

    // --- Product Management Operations ---

    /**
     * Adds a product to the farmer's list of available products.
     * If a product with the same ID already exists, it updates the existing product.
     *
     * @param product The product to add or update.
     */
    public void addOrUpdateProduct(Product product) {
        if (product == null) {
            return; // Or throw an exception
        }
        // Check if product already exists by ID and update it
        boolean found = false;
        for (int i = 0; i < availableProducts.size(); i++) {
            if (availableProducts.get(i).getProductId().equals(product.getProductId())) {
                availableProducts.set(i, product); // Update existing product
                found = true;
                break;
            }
        }
        if (!found) {
            availableProducts.add(product); // Add new product
        }
    }

    /**
     * Removes a product from the farmer's list of available products.
     *
     * @param productId The ID of the product to remove.
     * @return true if the product was removed, false otherwise.
     */
    public boolean removeProduct(String productId) {
        // Iterate and remove by product ID
        for (int i = 0; i < availableProducts.size(); i++) {
            if (availableProducts.get(i).getProductId().equals(productId)) {
                availableProducts.remove(i);
                return true;
            }
        }
        return false;
    }

    // --- Implementations of Abstract User Methods ---

    @Override
    public boolean authenticate(String enteredPassword) {
        return this.password.equals(enteredPassword);
    }

    @Override
    public boolean checkServiceEligibility() {
        // Farmers are always eligible to provide products if they have a farm address.
        return farmAddress != null && !farmAddress.trim().isEmpty();
    }

    @Override
    public String getNotificationPreferences() {
        // For simplicity, assume farmers prefer email notifications.
        return "email";
    }

    @Override
    public String toString() {
        return "Farmer ID: " + userId + ", Name: " + name + ", Email: " + email + ", Farm Address: " + farmAddress;
    }
}