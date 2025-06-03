package com.onlinemarketplace.gui;

import com.onlinemarketplace.exception.HarvestDateException;
import com.onlinemarketplace.exception.ValidationException;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.Order;
import com.onlinemarketplace.model.OrderItem;
import com.onlinemarketplace.model.Product;
import com.onlinemarketplace.service.DeliverySystem;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
// Removed: Optional, Collectors

/**
 * GUI component for the Farmer Dashboard.
 * Allows farmers to manage their products (add/update/delete harvest schedules and quantities)
 * and view orders placed by customers for their products.
 * Simulates photo uploads.
 */
public class FarmerDashboardGUI {

    private final DeliverySystem deliverySystem;
    private final Farmer currentFarmer; // The logged-in farmer
    private final LogoutCallback logoutAction; // Changed to custom interface
    private ScrollPane view;

    // Product Management Form fields
    private TextField productIdField;
    private TextField productNameField;
    private TextField productDescriptionField;
    private TextField productPriceField;
    private TextField productQuantityField;
    private DatePicker harvestDateField;
    private Label productMessageLabel;
    private ListView<Product> farmerProductsList;

    // Order View
    private TableView<Order> farmerOrdersTable;
    private Label orderMessageLabel;


    /**
     * Constructs a FarmerDashboardGUI.
     *
     * @param deliverySystem The core delivery system instance.
     * @param currentFarmer The Farmer object currently logged in.
     * @param logoutAction A LogoutCallback to execute when the user logs out.
     */
    public FarmerDashboardGUI(DeliverySystem deliverySystem, Farmer currentFarmer, LogoutCallback logoutAction) {
        this.deliverySystem = deliverySystem;
        this.currentFarmer = currentFarmer;
        this.logoutAction = logoutAction;
        initializeGUI();
        populateFarmerProductsList(); // Ensure products are loaded and displayed
        refreshFarmerOrdersTable();
    }

    /**
     * Initializes the GUI components and layout for the farmer dashboard.
     */
    private void initializeGUI() { // Changed to void, was accidentally 'private void void initializeGUI()'
        view = new ScrollPane(); // Increased spacing
        view.setFitToWidth(true);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #e0ffe0;"); // Light green background
        VBox.setVgrow(view, Priority.ALWAYS); // Allow the main VBox to grow

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("Welcome, " + currentFarmer.getName() + " (Farmer ID: " + currentFarmer.getUserId() + ")");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;"); // Darker green
        HBox.setHgrow(welcomeLabel, Priority.ALWAYS); // Allow label to push logout button to right

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        logoutBtn.setOnAction(e -> logoutAction.onLogout()); // Changed method call
        headerBox.getChildren().addAll(welcomeLabel, logoutBtn);


        // --- Product Management Section ---
        TitledPane productManagementPane = new TitledPane();
        productManagementPane.setText("Product Management");
        productManagementPane.setCollapsible(false);
        productManagementPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #d0f0d0;");
        VBox.setVgrow(productManagementPane, Priority.ALWAYS); // Allow TitledPane to grow

        VBox productContent = new VBox(15);
        productContent.setPadding(new Insets(15));
        productContent.setStyle("-fx-background-color: white; -fx-border-color: #c0e0c0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        VBox.setVgrow(productContent, Priority.ALWAYS); // Allow content VBox to grow

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        int row = 0;
        formGrid.add(new Label("Product ID:"), 0, row);
        productIdField = new TextField();
        productIdField.setPromptText("Unique ID (e.g., P001)");
        formGrid.add(productIdField, 1, row++);

        formGrid.add(new Label("Product Name:"), 0, row);
        productNameField = new TextField();
        productNameField.setPromptText("e.g., Organic Carrots");
        formGrid.add(productNameField, 1, row++);

        formGrid.add(new Label("Description:"), 0, row);
        productDescriptionField = new TextField();
        productDescriptionField.setPromptText("e.g., Organic, Seasonal, Vegetable");
        formGrid.add(productDescriptionField, 1, row++);

        formGrid.add(new Label("Price ($):"), 0, row);
        productPriceField = new TextField();
        productPriceField.setPromptText("e.g., 2.50");
        formGrid.add(productPriceField, 1, row++);

        formGrid.add(new Label("Quantity Available:"), 0, row);
        productQuantityField = new TextField();
        productQuantityField.setPromptText("e.g., 100");
        formGrid.add(productQuantityField, 1, row++);

        formGrid.add(new Label("Harvest Date:"), 0, row);
        harvestDateField = new DatePicker(LocalDate.now());
        formGrid.add(harvestDateField, 1, row++);

        HBox productButtons = new HBox(10);
        productButtons.setAlignment(Pos.CENTER_RIGHT);
        Button addProductButton = new Button("Add/Update Product");
        addProductButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addProductButton.setOnAction(e -> handleAddOrUpdateProduct());

        Button deleteProductButton = new Button("Delete Selected Product");
        deleteProductButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        deleteProductButton.setOnAction(e -> handleDeleteProduct());

        Button uploadPhotoButton = new Button("Upload Harvest Photo (Simulated)");
        uploadPhotoButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        uploadPhotoButton.setOnAction(e -> productMessageLabel.setText("Photo upload simulated. In a real app, this would open a file dialog."));

        productButtons.getChildren().addAll(addProductButton, deleteProductButton, uploadPhotoButton);

        productMessageLabel = new Label("");
        productMessageLabel.setWrapText(true);
        productMessageLabel.setStyle("-fx-font-weight: bold;");

        Label currentProductsLabel = new Label("Your Current Product Listings:");
        currentProductsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        farmerProductsList = new ListView<>();
        farmerProductsList.setPrefHeight(150); // Set a reasonable preferred height
        VBox.setVgrow(farmerProductsList, Priority.ALWAYS); // Allow list to grow

        // Add a listener to populate fields when a product is selected
        farmerProductsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateFormWithProduct(newVal);
            } else {
                clearFormFields();
            }
        });

        productContent.getChildren().addAll(formGrid, productButtons, productMessageLabel, currentProductsLabel, farmerProductsList);
        productManagementPane.setContent(productContent);

        // --- Customer Orders Section ---
        TitledPane customerOrdersPane = new TitledPane();
        customerOrdersPane.setText("Customer Orders for Your Products");
        customerOrdersPane.setCollapsible(false);
        customerOrdersPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #d0f0d0;");
        VBox.setVgrow(customerOrdersPane, Priority.ALWAYS); // Allow TitledPane to grow

        VBox orderContent = new VBox(10);
        orderContent.setPadding(new Insets(15));
        orderContent.setStyle("-fx-background-color: white; -fx-border-color: #c0e0c0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        VBox.setVgrow(orderContent, Priority.ALWAYS); // Allow content VBox to grow

        farmerOrdersTable = new TableView<>();
        farmerOrdersTable.setPlaceholder(new Label("No orders found for your products."));
        farmerOrdersTable.setPrefHeight(150); // Set a reasonable preferred height
        VBox.setVgrow(farmerOrdersTable, Priority.ALWAYS);

        // Define columns for the orders table
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        orderIdCol.setPrefWidth(100);

        TableColumn<Order, String> customerIdCol = new TableColumn<>("Customer ID");
        customerIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerId()));
        customerIdCol.setPrefWidth(100);

        TableColumn<Order, String> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
        orderDateCol.setPrefWidth(120);

        TableColumn<Order, String> productsCol = new TableColumn<>("Products Ordered");
        productsCol.setCellValueFactory(cellData -> {
            StringBuilder sb = new StringBuilder();
            // Iterate through OrderItems
            for (OrderItem item : cellData.getValue().getOrderedItems()) {
                Product p = deliverySystem.getProduct(item.getProductId());
                // Only show *this* farmer's products if they are part of this order
                if (p != null && p.getFarmerId().equals(currentFarmer.getUserId())) {
                     sb.append(p.getName()).append(" (x").append(item.getQuantity()).append("), ");
                }
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // Remove trailing ", "
            }
            return new SimpleStringProperty(sb.toString());
        });
        productsCol.setPrefWidth(250);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total Amount");
        totalCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotalAmount())));
        totalCol.setPrefWidth(100);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        statusCol.setPrefWidth(100);

        farmerOrdersTable.getColumns().addAll(orderIdCol, customerIdCol, orderDateCol, productsCol, totalCol, statusCol);

        orderMessageLabel = new Label("");
        orderMessageLabel.setWrapText(true);
        orderMessageLabel.setStyle("-fx-font-weight: bold;");

        orderContent.getChildren().addAll(farmerOrdersTable, orderMessageLabel);
        customerOrdersPane.setContent(orderContent);

        view.setContent(new VBox(10, headerBox, productManagementPane, customerOrdersPane));

        // Set VGrow for the TitledPanes within the main VBox
        VBox.setVgrow(productManagementPane, Priority.ALWAYS);
        VBox.setVgrow(customerOrdersPane, Priority.ALWAYS);
    }


    /**
     * Populates the ListView with the current farmer's available products.
     */
    private void populateFarmerProductsList() {
        farmerProductsList.getItems().clear();
        if (currentFarmer != null) {
            // Ensure the farmer's products are up-to-date from the delivery system's master list
            // This handles cases where products might have been loaded from file or updated by system
            List<Product> productsFromSystem = deliverySystem.getAllProducts();
            List<Product> farmersProducts = new ArrayList<>();
            for (Product p : productsFromSystem) {
                if (p.getFarmerId().equals(currentFarmer.getUserId())) {
                    farmersProducts.add(p);
                }
            }
            farmerProductsList.getItems().addAll(farmersProducts);
        }
    }

    /**
     * Populates the product form fields with details from a selected product.
     *
     * @param product The product to display in the form.
     */
    private void populateFormWithProduct(Product product) {
        productIdField.setText(product.getProductId());
        productNameField.setText(product.getName());
        productDescriptionField.setText(product.getDescription());
        productPriceField.setText(String.valueOf(product.getPrice()));
        productQuantityField.setText(String.valueOf(product.getQuantityAvailable()));
        harvestDateField.setValue(product.getHarvestDate());
    }

    /**
     * Clears all input fields in the product form.
     */
    private void clearFormFields() {
        productIdField.clear();
        productNameField.clear();
        productDescriptionField.clear();
        productPriceField.clear();
        productQuantityField.clear();
        harvestDateField.setValue(LocalDate.now());
        farmerProductsList.getSelectionModel().clearSelection();
    }

    /**
     * Handles the action of adding or updating a product.
     * Validates input and updates the DeliverySystem.
     */
    private void handleAddOrUpdateProduct() {
        String productId = productIdField.getText().trim();
        String productName = productNameField.getText().trim();
        String productDescription = productDescriptionField.getText().trim();
        String priceText = productPriceField.getText().trim();
        String quantityText = productQuantityField.getText().trim();
        LocalDate harvestDate = harvestDateField.getValue();

        if (productId.isEmpty() || productName.isEmpty() || priceText.isEmpty() || quantityText.isEmpty() || harvestDate == null) {
            productMessageLabel.setText("Please fill in all product fields.");
            productMessageLabel.setTextFill(Color.RED);
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            Product newProduct = new Product(productId, productName, productDescription, price, quantity, harvestDate, currentFarmer.getUserId());
            deliverySystem.addProduct(newProduct); // This will also update the farmer's product list
            productMessageLabel.setText("Product '" + productName + "' added/updated successfully!");
            productMessageLabel.setTextFill(Color.GREEN);
            populateFarmerProductsList(); // Refresh the list
            clearFormFields();
        } catch (NumberFormatException e) {
            productMessageLabel.setText("Invalid number format for Price or Quantity.");
            productMessageLabel.setTextFill(Color.RED);
        } catch (DateTimeParseException e) {
            productMessageLabel.setText("Invalid date format for Harvest Date.");
            productMessageLabel.setTextFill(Color.RED);
        } catch (ValidationException | HarvestDateException e) {
            productMessageLabel.setText("Error: " + e.getMessage());
            productMessageLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Handles deleting a selected product.
     */
    private void handleDeleteProduct() {
        Product selectedProduct = farmerProductsList.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            productMessageLabel.setText("Please select a product to delete.");
            productMessageLabel.setTextFill(Color.RED);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Product: " + selectedProduct.getName());
        alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");

        // Replace Optional with null check
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL); // orElse provides a default if no button is clicked
        if (result == ButtonType.OK) {
            if (deliverySystem.removeProduct(selectedProduct.getProductId())) {
                productMessageLabel.setText("Product '" + selectedProduct.getName() + "' deleted successfully.");
                productMessageLabel.setTextFill(Color.GREEN);
                populateFarmerProductsList(); // Refresh the list
                clearFormFields();
            } else {
                productMessageLabel.setText("Failed to delete product '" + selectedProduct.getName() + "'.");
                productMessageLabel.setTextFill(Color.RED);
            }
        }
    }

    /**
     * Refreshes the table view with orders relevant to the current farmer.
     */
    private void refreshFarmerOrdersTable() {
        if (currentFarmer != null) {
            List<Order> orders = deliverySystem.getOrdersForFarmer(currentFarmer.getUserId());
            farmerOrdersTable.getItems().setAll(orders);
            if (orders.isEmpty()) {
                orderMessageLabel.setText("No orders found for your products.");
                orderMessageLabel.setTextFill(Color.BLACK);
            } else {
                orderMessageLabel.setText(""); // Clear message if orders are present
            }
        }
    }

    /**
     * Returns the VBox containing the entire Farmer Dashboard GUI.
     *
     * @return The VBox view.
     */
    public ScrollPane getView() {
        return view;
    }
}