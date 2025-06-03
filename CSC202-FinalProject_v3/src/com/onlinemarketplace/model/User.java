package com.onlinemarketplace.model;

import com.onlinemarketplace.exception.ValidationException;

import java.util.Objects;

/**
 * Abstract base class for all users in the Online Marketplace System.
 * Defines common attributes and abstract methods that concrete user types (Customer, Farmer) must implement.
 * Encapsulation is applied to all data members.
 */
public abstract class User {
    protected String userId;
    protected String name;
    protected String email;
    protected String password; // Simple password for authentication simulation
    protected String userType; // "Customer" or "Farmer"

    /**
     * Constructs a new User instance.
     *
     * @param userId The unique identifier for the user.
     * @param name The name of the user.
     * @param email The email address of the user.
     * @param password The password for user authentication.
     * @param userType The type of user ("Customer" or "Farmer").
     * @throws ValidationException If email is invalid.
     */
    public User(String userId, String name, String email, String password, String userType) throws ValidationException {
        this.userId = userId;
        this.name = name;
        setEmail(email); // Use setter for validation
        this.password = password;
        this.userType = userType;
    }

    // --- Getters ---

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

    // --- Setters with Validation ---

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the email address for the user.
     *
     * @param email The new email address.
     * @throws ValidationException If the email format is invalid.
     */
    public void setEmail(String email) throws ValidationException {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Invalid email format.");
        }
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // --- Abstract Methods ---

    /**
     * Abstract method for user authentication.
     *
     * @param enteredPassword The password entered by the user.
     * @return true if authentication is successful, false otherwise.
     */
    public abstract boolean authenticate(String enteredPassword);

    /**
     * Abstract method to check service eligibility based on location.
     * Concrete implementations will define what constitutes eligibility.
     *
     * @return true if the user is eligible for service, false otherwise.
     */
    public abstract boolean checkServiceEligibility();

    /**
     * Abstract method to retrieve notification preferences.
     *
     * @return A string representing notification preferences (e.g., "email", "sms", "none").
     */
    public abstract String getNotificationPreferences();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}