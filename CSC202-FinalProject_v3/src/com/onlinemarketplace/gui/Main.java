package com.onlinemarketplace.gui;

import com.onlinemarketplace.model.User;
import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.service.DeliverySystem;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

// Removed: import java.util.function.Consumer;

/**
 * Main class for the Online Marketplace System GUI.
 * This class sets up the primary stage and manages the flow between
 * the login page and the main application dashboard (TabPane).
 */
public class Main extends Application {

    private DeliverySystem deliverySystem;
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainAppScene;
    private TabPane mainTabPane;

    /**
     * The entry point for the JavaFX application.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.deliverySystem = new DeliverySystem(); // Initialize DeliverySystem, loads data

        // Set up the login page
        // Pass custom interfaces for login success and logout
        LoginPageGUI loginPage = new LoginPageGUI(deliverySystem, new LoginSuccessCallback() {
            @Override
            public void onLoginSuccess(User user) {
                Main.this.onLoginSuccess(user); // Use Main.this to refer to the outer class instance
            }
        }, new LogoutCallback() {
            @Override
            public void onLogout() {
                Main.this.showLoginPage(); // Use Main.this
            }
        });
        loginScene = new Scene(loginPage.getView(), 800, 600); // Smaller scene for login

        primaryStage.setTitle("Online Marketplace System - Login");
        primaryStage.setScene(loginScene);
        primaryStage.show();

        // Save data when the application closes
        primaryStage.setOnCloseRequest(event -> deliverySystem.saveData());
    }

    /**
     * Callback method executed upon successful login.
     * Switches the scene to the main application dashboard based on user type.
     *
     * @param user The authenticated User object (Customer or Farmer).
     */
    private void onLoginSuccess(User user) {
        mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pass the logout action to the dashboards using the custom interface
        LogoutCallback logoutAction = new LogoutCallback() {
            @Override
            public void onLogout() {
                Main.this.showLoginPage();
            }
        };

        if (user instanceof Farmer) {
            // Farmer dashboard
            Tab farmerTab = new Tab("Farmer Dashboard");
            farmerTab.setContent(new FarmerDashboardGUI(deliverySystem, (Farmer) user, logoutAction).getView());
            mainTabPane.getTabs().add(farmerTab);
            primaryStage.setTitle("Online Marketplace System - Farmer Dashboard");
        } else if (user instanceof Customer) {
            // Customer portal
            Tab customerTab = new Tab("Customer Portal");
            customerTab.setContent(new CustomerPortalGUI(deliverySystem, (Customer) user, logoutAction).getView());
            mainTabPane.getTabs().add(customerTab);
            primaryStage.setTitle("Online Marketplace System - Customer Portal");

            // Also add Delivery Tracking for customers
            Tab deliveryTab = new Tab("Delivery Tracking");
            deliveryTab.setContent(new DeliveryTrackingGUI(deliverySystem).getView());
            mainTabPane.getTabs().add(deliveryTab);
        }

        // Create the main application scene
        mainAppScene = new Scene(mainTabPane, 1200, 800); // Larger scene for main app
        primaryStage.setScene(mainAppScene);
        primaryStage.centerOnScreen(); // Center the window
    }

    /**
     * Switches the scene back to the login page.
     * This method is passed as a callback to the dashboard GUIs for logout functionality.
     */
    private void showLoginPage() {
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Online Marketplace System - Login");
        primaryStage.centerOnScreen();
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}