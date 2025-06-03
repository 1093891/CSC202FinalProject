package com.onlinemarketplace.model;

import com.onlinemarketplace.exception.ValidationException;

import java.util.ArrayList;
import java.util.List; // Using List interface, implemented by ArrayList

/**
 * Represents a customer in the Online Marketplace System.
 * Inherits from the abstract User class and includes customer-specific details
 * like delivery address and a shopping cart.
 */
public class Customer extends User {
    private String deliveryAddress;
    private List<ShoppingCartItem> shoppingCart; // Changed from Map to List of ShoppingCartItem

    /**
     * Constructs a new Customer instance.
     *
     * @param customerId The unique identifier for the customer.
     * @param name The name of the customer.
     * @param email The email address of the customer.
     * @param password The password for customer authentication.
     * @param deliveryAddress The delivery address for the customer.
     * @throws ValidationException If email or delivery address is invalid.
     */
    public Customer(String customerId, String name, String email, String password, String deliveryAddress)
            throws ValidationException {
        super(customerId, name, email, password, "Customer"); // Set user type
        setDeliveryAddress(deliveryAddress); // Use setter for validation
        this.shoppingCart = new ArrayList<>(); // Initialize as ArrayList
    }

    // --- Getters ---

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public List<ShoppingCartItem> getShoppingCart() {
        return shoppingCart;
    }

    // --- Setters with Validation ---

    /**
     * Sets the delivery address for the customer.
     *
     * @param deliveryAddress The new delivery address.
     * @throws ValidationException If the delivery address is null or empty.
     */
    public void setDeliveryAddress(String deliveryAddress) throws ValidationException {
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new ValidationException("Delivery address cannot be empty.");
        }
        this.deliveryAddress = deliveryAddress;
    }

    // --- Shopping Cart Operations ---

    /**
     * Adds a product to the shopping cart or updates its quantity if already present.
     *
     * @param product The product to add.
     * @param quantity The quantity of the product to add.
     * @throws ValidationException If the quantity is not positive.
     */
    public void addToCart(Product product, int quantity) throws ValidationException {
        if (product == null) {
            throw new ValidationException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive to add to cart.");
        }

        boolean found = false;
        for (int i = 0; i < shoppingCart.size(); i++) {
            ShoppingCartItem item = shoppingCart.get(i);
            if (item.getProduct().equals(product)) {
                item.addQuantity(quantity); // Update quantity of existing item
                found = true;
                break;
            }
        }
        if (!found) {
            shoppingCart.add(new ShoppingCartItem(product, quantity)); // Add new item
        }
    }

    /**
     * Removes a product from the shopping cart.
     *
     * @param product The product to remove.
     */
    public void removeFromCart(Product product) {
        // Iterate and remove by product ID
        for (int i = 0; i < shoppingCart.size(); i++) {
            ShoppingCartItem item = shoppingCart.get(i);
            if (item.getProduct().equals(product)) {
                shoppingCart.remove(i);
                return; // Assuming only one entry per product
            }
        }
    }

    /**
     * Clears all items from the shopping cart.
     */
    public void clearCart() {
        shoppingCart.clear();
    }

    // --- Implementations of Abstract User Methods ---

    @Override
    public boolean authenticate(String enteredPassword) {
        return this.password.equals(enteredPassword);
    }

    @Override
    public boolean checkServiceEligibility() {
        // Simple eligibility check: assume any non-empty address is eligible.
        // In a real system, this would involve geocoding and checking against service areas.
        return deliveryAddress != null && !deliveryAddress.trim().isEmpty();
    }

    @Override
    public String getNotificationPreferences() {
        // For simplicity, assume customers prefer email notifications.
        return "email";
    }

    @Override
    public String toString() {
        return "Customer ID: " + userId + ", Name: " + name + ", Email: " + email + ", Address: " + deliveryAddress;
    }
}
