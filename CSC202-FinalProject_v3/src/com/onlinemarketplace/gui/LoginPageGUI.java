package com.onlinemarketplace.gui;

import com.onlinemarketplace.exception.ValidationException;
import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.User;
import com.onlinemarketplace.service.DeliverySystem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

// Removed: import java.util.function.Consumer;

/**
 * GUI for user login and registration.
 * Allows users to sign in to existing accounts or create new ones.
 */
public class LoginPageGUI {

    private final DeliverySystem deliverySystem;
    private final LoginSuccessCallback onLoginSuccess; // Changed to custom interface
    private final LogoutCallback onLogoutSuccess; // Changed to custom interface

    private VBox view;
    private TextField userIdField;
    private PasswordField passwordField;
    private Label messageLabel;

    // Registration fields
    private TextField regNameField;
    private TextField regEmailField;
    private PasswordField regPasswordField;
    private PasswordField regConfirmPasswordField;
    private TextField regAddressField;
    private ComboBox<String> regUserTypeSelector;
    private VBox registerSection; // Added for registration section

    /**
     * Constructs a LoginPageGUI.
     *
     * @param deliverySystem The core delivery system for authentication and registration.
     * @param onLoginSuccess Callback to execute upon successful login, passing the authenticated User.
     * @param onLogoutSuccess Callback to execute upon logout, returning to this page.
     */
    public LoginPageGUI(DeliverySystem deliverySystem, LoginSuccessCallback onLoginSuccess, LogoutCallback onLogoutSuccess) {
        this.deliverySystem = deliverySystem;
        this.onLoginSuccess = onLoginSuccess;
        this.onLogoutSuccess = onLogoutSuccess; // Store the logout callback
        initializeGUI();
    }

    /**
     * Initializes the GUI components and layout for the login page.
     */
    private void initializeGUI() {
        view = new VBox(15);
        view.setPadding(new Insets(30));
        view.setAlignment(Pos.CENTER);
        view.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px;");

        Label title = new Label("Online Marketplace Login");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Login Section
        VBox loginSection = new VBox(10);
        loginSection.setAlignment(Pos.CENTER);
        loginSection.setStyle("-fx-padding: 20; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-color: white;");

        Label loginHeader = new Label("Sign In");
        loginHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        GridPane loginGrid = new GridPane();
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setAlignment(Pos.CENTER);

        userIdField = new TextField();
        userIdField.setPromptText("User ID (e.g., C001 or F001)");
        userIdField.setPrefWidth(250);
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(250);

        loginGrid.add(new Label("User ID:"), 0, 0);
        loginGrid.add(userIdField, 1, 0);
        loginGrid.add(new Label("Password:"), 0, 1);
        loginGrid.add(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 20; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> handleLogin());

        Button gotoRegisterButton = new Button("Create New Account");
        gotoRegisterButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 15; -fx-background-radius: 5;");
        gotoRegisterButton.setOnAction(e -> {
            // Switch to registration section
            view.getChildren().clear();
            view.getChildren().addAll(title, registerSection, messageLabel);
        });

        HBox buttonBox = new HBox(10, gotoRegisterButton, loginButton);
        buttonBox.setAlignment(Pos.CENTER);
        loginGrid.add(buttonBox, 1, 2);
        loginGrid.setPadding(new Insets(10));
        loginGrid.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        loginSection.getChildren().addAll(loginHeader, loginGrid);

        // Registration Section
        registerSection = new VBox(10);
        registerSection.setAlignment(Pos.CENTER);
        registerSection.setStyle("-fx-padding: 20; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-color: white;");

        Label registerHeader = new Label("Create New Account");
        registerHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        GridPane regGrid = new GridPane();
        regGrid.setHgap(10);
        regGrid.setVgap(10);
        regGrid.setAlignment(Pos.CENTER);

        regNameField = new TextField();
        regNameField.setPromptText("Your Name");
        regEmailField = new TextField();
        regEmailField.setPromptText("Email");
        regPasswordField = new PasswordField();
        regPasswordField.setPromptText("Password");
        regConfirmPasswordField = new PasswordField();
        regConfirmPasswordField.setPromptText("Confirm Password");
        regAddressField = new TextField();
        regAddressField.setPromptText("Delivery/Farm Address");
        regUserTypeSelector = new ComboBox<>();
        regUserTypeSelector.getItems().addAll("Customer", "Farmer");
        regUserTypeSelector.setPromptText("Account Type");

        int row = 0;
        regGrid.add(new Label("Name:"), 0, row);
        regGrid.add(regNameField, 1, row++);
        regGrid.add(new Label("Email:"), 0, row);
        regGrid.add(regEmailField, 1, row++);
        regGrid.add(new Label("Password:"), 0, row);
        regGrid.add(regPasswordField, 1, row++);
        regGrid.add(new Label("Confirm Password:"), 0, row);
        regGrid.add(regConfirmPasswordField, 1, row++);
        regGrid.add(new Label("Address:"), 0, row);
        regGrid.add(regAddressField, 1, row++);
        regGrid.add(new Label("Account Type:"), 0, row);
        regGrid.add(regUserTypeSelector, 1, row++);

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #007BFF; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 8 20; -fx-background-radius: 5;");
        registerButton.setOnAction(e -> handleRegister());

        Button gotoLoginButton = new Button("Back to Login");
        gotoLoginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 15; -fx-background-radius: 5;");
        gotoLoginButton.setOnAction(e -> {
            // Switch back to login section
            view.getChildren().clear();
            view.getChildren().addAll(title, loginSection, messageLabel);
        });
        HBox regButtonBox = new HBox(10, gotoLoginButton, registerButton);
        regButtonBox.setAlignment(Pos.CENTER);
        regGrid.add(regButtonBox, 1, row);
        regGrid.setPadding(new Insets(10));
        regGrid.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        registerSection.getChildren().addAll(registerHeader, regGrid);

        messageLabel = new Label("");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setStyle("-fx-font-weight: bold;");

        view.getChildren().addAll(title, loginSection, messageLabel);
    }

    /**
     * Handles the login attempt.
     */
    private void handleLogin() {
        String userId = userIdField.getText().trim();
        String password = passwordField.getText();

        if (userId.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter User ID and Password.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        User authenticatedUser = deliverySystem.authenticateUser(userId, password);
        if (authenticatedUser != null) {
            messageLabel.setText("Login successful! Welcome, " + authenticatedUser.getName() + "!");
            messageLabel.setTextFill(Color.GREEN);
            onLoginSuccess.onLoginSuccess(authenticatedUser); // Changed method call
            clearLoginFields(); // Clear fields after successful login
        } else {
            messageLabel.setText("Login failed. Invalid User ID or Password.");
            messageLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Clears the login form fields.
     */
    private void clearLoginFields() {
        userIdField.clear();
        passwordField.clear();
    }

    /**
     * Handles the new user registration attempt.
     */
    private void handleRegister() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = regPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();
        String address = regAddressField.getText().trim();
        String userType = regUserTypeSelector.getSelectionModel().getSelectedItem();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty() || userType == null) {
            messageLabel.setText("Please fill in all registration fields.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match.");
            messageLabel.setTextFill(Color.RED);
            return;
        }

        // Generate a simple unique ID for the new user.
        // This is simplified. In a real system, you'd check for uniqueness more robustly.
        String newUserIdPrefix = userType.equals("Customer") ? "C" : "F";
        String newUserId = "";
        // Basic loop to find a unique ID
        int counter = 1;
        while (true) {
            String potentialId = newUserIdPrefix + String.format("%03d", counter);
            if (deliverySystem.getUser(potentialId) == null) { // Check if ID exists
                newUserId = potentialId;
                break;
            }
            counter++;
        }


        try {
            User newUser;
            if (userType.equals("Customer")) {
                newUser = new Customer(newUserId, name, email, password, address);
            } else { // Farmer
                newUser = new Farmer(newUserId, name, email, password, address);
            }

            if (deliverySystem.registerUser(newUser)) {
                messageLabel.setText("Account created successfully for " + name + " (ID: " + newUserId + "). You can now log in.");
                messageLabel.setTextFill(Color.GREEN);
                clearRegistrationFields();
            } else {
                messageLabel.setText("Registration failed. User ID might already exist or other issue.");
                messageLabel.setTextFill(Color.RED);
            }
        } catch (ValidationException e) {
            messageLabel.setText("Registration error: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
        } catch (Exception e) {
            messageLabel.setText("An unexpected error occurred during registration: " + e.getMessage());
            messageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Clears the registration form fields.
     */
    private void clearRegistrationFields() {
        regNameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regAddressField.clear();
        regUserTypeSelector.getSelectionModel().clearSelection();
    }

    /**
     * Returns the VBox containing the entire Login Page GUI.
     *
     * @return The VBox view.
     */
    public VBox getView() {
        return view;
    }
}