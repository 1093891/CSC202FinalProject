package com.onlinemarketplace.gui;

import com.onlinemarketplace.exception.DeliveryUnavailableException;
import com.onlinemarketplace.exception.OutOfSeasonException;
import com.onlinemarketplace.exception.ValidationException;
import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.Order;
import com.onlinemarketplace.model.OrderItem;
import com.onlinemarketplace.model.Product;
import com.onlinemarketplace.model.ShoppingCartItem;
import com.onlinemarketplace.model.Subscription;
import com.onlinemarketplace.service.DeliverySystem;
import com.onlinemarketplace.service.IProductSearch;
import com.onlinemarketplace.service.ProductSearchEngine;
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
import java.util.ArrayList;
import java.util.List;
// Removed: Optional, Map, Collectors (already removed in previous step)

/**
 * GUI component for the Customer Portal.
 * Allows customers to browse products, search, add to cart, place orders,
 * view order history, cancel orders, and subscribe to farmers.
 */
public class CustomerPortalGUI {

    private final DeliverySystem deliverySystem;
    private final ProductSearchEngine productSearchEngine;
    private final Customer currentCustomer; // The logged-in customer
    private final LogoutCallback logoutAction; // Changed to custom interface
    private ScrollPane view;

    // UI elements for Product Browsing/Cart
    private ListView<Product> productListView;
    private ListView<String> shoppingCartView;
    private Label productMessageLabel;
    private Label cartTotalLabel;
    private TextField searchField;
    private DatePicker seasonDatePicker;
    private TextField proximityRadiusField;
    private TextField categoryField;

    // UI elements for Order History
    private TableView<Order> orderHistoryTable;
    private Label orderHistoryMessageLabel;

    // UI elements for Subscriptions
    private ListView<Farmer> farmerSubscriptionListView; // For selecting farmer to subscribe
    private ListView<Subscription> currentSubscriptionsListView; // For viewing current subscriptions
    private Label subscriptionMessageLabel;


    /**
     * Constructs a CustomerPortalGUI.
     *
     * @param deliverySystem The core delivery system instance.
     * @param currentCustomer The Customer object currently logged in.
     * @param logoutAction A LogoutCallback to execute when the user logs out.
     */
    public CustomerPortalGUI(DeliverySystem deliverySystem, Customer currentCustomer, LogoutCallback logoutAction) {
        this.deliverySystem = deliverySystem;
        this.productSearchEngine = (ProductSearchEngine) deliverySystem.getProductSearchEngine();
        this.currentCustomer = currentCustomer;
        this.logoutAction = logoutAction;
        initializeGUI();
        refreshProductList(deliverySystem.getAllProducts()); // Show all products initially
        refreshShoppingCart();
        refreshOrderHistoryTable();
        refreshFarmerSubscriptionList();
        refreshCurrentSubscriptionsList();
    }

    /**
     * Initializes the GUI components and layout for the customer portal.
     */
    private void initializeGUI() {
        view = new ScrollPane(); // Increased spacing

        view.setFitToWidth(true); // Fit content to width


        VBox.setVgrow(view, Priority.ALWAYS); // Allow the ScrollPane to grow
        VBox.setVgrow(view, Priority.ALWAYS); // Allow the main VBox to grow

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label welcomeLabel = new Label("Welcome, " + currentCustomer.getName() + " (Customer ID: " + currentCustomer.getUserId() + ")");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1e90ff;"); // Dodger blue
        HBox.setHgrow(welcomeLabel, Priority.ALWAYS); // Allow label to push logout button to right

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #FF6347; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        logoutBtn.setOnAction(e -> logoutAction.onLogout()); // Changed method call
        headerBox.getChildren().addAll(welcomeLabel, logoutBtn);


        // --- Product Browsing and Cart Section ---
        TitledPane productBrowsingPane = new TitledPane();
        productBrowsingPane.setText("Product Catalog & Shopping Cart");
        productBrowsingPane.setCollapsible(false);
        productBrowsingPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #d0e0f0;");
        VBox.setVgrow(productBrowsingPane, Priority.ALWAYS); // Allow TitledPane to grow

        VBox productContent = new VBox(15);
        productContent.setPadding(new Insets(15));
        productContent.setStyle("-fx-background-color: white; -fx-border-color: #c0d0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        VBox.setVgrow(productContent, Priority.ALWAYS); // Allow content VBox to grow

        // Search and Filter controls
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(10);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(10));

        int row = 0;
        searchGrid.add(new Label("Search:"), 0, row);
        searchField = new TextField();
        searchField.setPromptText("Name/Description");
        searchField.setPrefWidth(200);
        searchGrid.add(searchField, 1, row);
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: white; -fx-background-radius: 5;");
        searchButton.setOnAction(e -> refreshProductList(productSearchEngine.searchProducts(searchField.getText())));
        searchGrid.add(searchButton, 2, row++);

        searchGrid.add(new Label("Season:"), 0, row);
        seasonDatePicker = new DatePicker(LocalDate.now());
        searchGrid.add(seasonDatePicker, 1, row);
        Button searchSeasonButton = new Button("Search by Season");
        searchSeasonButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: white; -fx-background-radius: 5;");
        searchSeasonButton.setOnAction(e -> refreshProductList(productSearchEngine.searchBySeason(seasonDatePicker.getValue())));
        searchGrid.add(searchSeasonButton, 2, row++);

        searchGrid.add(new Label("Proximity (km):"), 0, row);
        proximityRadiusField = new TextField("50");
        proximityRadiusField.setPrefWidth(80);
        searchGrid.add(proximityRadiusField, 1, row);
        Button searchProximityButton = new Button("Search by Proximity");
        searchProximityButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: white; -fx-background-radius: 5;");
        searchProximityButton.setOnAction(e -> {
            try {
                int radius = Integer.parseInt(proximityRadiusField.getText());
                refreshProductList(productSearchEngine.searchByProximity(currentCustomer.getDeliveryAddress(), radius));
            } catch (NumberFormatException ex) {
                productMessageLabel.setText("Invalid radius. Please enter a number.");
                productMessageLabel.setTextFill(Color.RED);
            }
        });
        searchGrid.add(searchProximityButton, 2, row++);

        searchGrid.add(new Label("Category:"), 0, row);
        categoryField = new TextField();
        categoryField.setPromptText("e.g., vegetable, organic");
        searchGrid.add(categoryField, 1, row);
        Button searchCategoryButton = new Button("Search by Category");
        searchCategoryButton.setStyle("-fx-background-color: #6495ED; -fx-text-fill: white; -fx-background-radius: 5;");
        searchCategoryButton.setOnAction(e -> refreshProductList(productSearchEngine.searchByCategory(categoryField.getText())));
        searchGrid.add(searchCategoryButton, 2, row++);

        productContent.getChildren().add(searchGrid);

        // Product List View
        Label availableProductsLabel = new Label("Available Products:");
        availableProductsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        productListView = new ListView<>();
        productListView.setPrefHeight(150); // Set a reasonable preferred height
        VBox.setVgrow(productListView, Priority.ALWAYS);

        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        addToCartButton.setOnAction(e -> handleAddToCart());

        // Shopping Cart View
        Label shoppingCartLabel = new Label("Shopping Cart:");
        shoppingCartLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        shoppingCartView = new ListView<>();
        shoppingCartView.setPrefHeight(100); // Set a reasonable preferred height
        VBox.setVgrow(shoppingCartView, Priority.ALWAYS);

        cartTotalLabel = new Label("Cart Total: $0.00");
        cartTotalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        HBox cartButtons = new HBox(10);
        cartButtons.setAlignment(Pos.CENTER_RIGHT);
        Button removeFromCartButton = new Button("Remove Selected");
        removeFromCartButton.setStyle("-fx-background-color: #FFC107; -fx-text-fill: black; -fx-background-radius: 5;");
        removeFromCartButton.setOnAction(e -> handleRemoveFromCart());
        Button clearCartButton = new Button("Clear Cart");
        clearCartButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-background-radius: 5;");
        clearCartButton.setOnAction(e -> handleClearCart());
        cartButtons.getChildren().addAll(removeFromCartButton, clearCartButton);

        Button placeOrderButton = new Button("Place Order");
        placeOrderButton.setStyle("-fx-background-color: #008CBA; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 10 20; -fx-background-radius: 8;");
        placeOrderButton.setOnAction(e -> handlePlaceOrder());

        productMessageLabel = new Label("");
        productMessageLabel.setWrapText(true);
        productMessageLabel.setStyle("-fx-font-weight: bold;");

        productContent.getChildren().addAll(availableProductsLabel, productListView, addToCartButton,
                shoppingCartLabel, shoppingCartView, cartTotalLabel, cartButtons, placeOrderButton, productMessageLabel);
        productBrowsingPane.setContent(productContent);

        // --- Order History Section ---
        TitledPane orderHistoryPane = new TitledPane();
        orderHistoryPane.setText("Your Order History");
        orderHistoryPane.setCollapsible(false);
        orderHistoryPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #d0e0f0;");
        VBox.setVgrow(orderHistoryPane, Priority.ALWAYS); // Allow TitledPane to grow

        VBox orderHistoryContent = new VBox(10);
        orderHistoryContent.setPadding(new Insets(15));
        orderHistoryContent.setStyle("-fx-background-color: white; -fx-border-color: #c0d0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        VBox.setVgrow(orderHistoryContent, Priority.ALWAYS); // Allow content VBox to grow

        orderHistoryTable = new TableView<>();
        orderHistoryTable.setPlaceholder(new Label("No past orders found."));
        orderHistoryTable.setPrefHeight(150); // Set a reasonable preferred height
        VBox.setVgrow(orderHistoryTable, Priority.ALWAYS);

        // Define columns for the order history table
        TableColumn<Order, String> ohOrderIdCol = new TableColumn<>("Order ID");
        ohOrderIdCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderId()));
        ohOrderIdCol.setPrefWidth(100);

        TableColumn<Order, String> ohOrderDateCol = new TableColumn<>("Order Date");
        ohOrderDateCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
        ohOrderDateCol.setPrefWidth(120);

        TableColumn<Order, String> ohProductsCol = new TableColumn<>("Products");
        ohProductsCol.setCellValueFactory(cellData -> {
            StringBuilder sb = new StringBuilder();
            // Iterate through OrderItems
            for (OrderItem item : cellData.getValue().getOrderedItems()) {
                Product p = deliverySystem.getProduct(item.getProductId());
                sb.append(p != null ? p.getName() : "Unknown Product").append(" (x").append(item.getQuantity()).append("), ");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 2); // Remove trailing ", "
            }
            return new SimpleStringProperty(sb.toString());
        });
        ohProductsCol.setPrefWidth(250);

        TableColumn<Order, String> ohTotalCol = new TableColumn<>("Total");
        ohTotalCol.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getTotalAmount())));
        ohTotalCol.setPrefWidth(80);

        TableColumn<Order, String> ohStatusCol = new TableColumn<>("Status");
        ohStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        ohStatusCol.setPrefWidth(100);

        orderHistoryTable.getColumns().addAll(ohOrderIdCol, ohOrderDateCol, ohProductsCol, ohTotalCol, ohStatusCol);

        Button cancelOrderButton = new Button("Cancel Selected Order");
        cancelOrderButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        cancelOrderButton.setOnAction(e -> handleCancelOrder());

        orderHistoryMessageLabel = new Label("");
        orderHistoryMessageLabel.setWrapText(true);
        orderHistoryMessageLabel.setStyle("-fx-font-weight: bold;");

        orderHistoryContent.getChildren().addAll(orderHistoryTable, cancelOrderButton, orderHistoryMessageLabel);
        orderHistoryPane.setContent(orderHistoryContent);

        // --- Farmer Subscription Section ---
        TitledPane subscriptionPane = new TitledPane();
        subscriptionPane.setText("Farmer Subscriptions");
        subscriptionPane.setCollapsible(false);
        subscriptionPane.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-background-color: #d0e0f0;");
        VBox.setVgrow(subscriptionPane, Priority.ALWAYS); // Allow TitledPane to grow

        VBox subscriptionContent = new VBox(10);
        subscriptionContent.setPadding(new Insets(15));
        subscriptionContent.setStyle("-fx-background-color: white; -fx-border-color: #c0d0e0; -fx-border-width: 1px; -fx-border-radius: 5px;");
        VBox.setVgrow(subscriptionContent, Priority.ALWAYS); // Allow content VBox to grow

        Label subscribeHeader = new Label("Subscribe to a Farmer:");
        subscribeHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        farmerSubscriptionListView = new ListView<>();
        farmerSubscriptionListView.setPrefHeight(100); // Set a reasonable preferred height
        VBox.setVgrow(farmerSubscriptionListView, Priority.ALWAYS);

        Button subscribeButton = new Button("Subscribe to Selected Farmer");
        subscribeButton.setStyle("-fx-background-color: #8BC34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        subscribeButton.setOnAction(e -> handleSubscribeToFarmer());

        Label currentSubscriptionsHeader = new Label("Your Current Subscriptions:");
        currentSubscriptionsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        currentSubscriptionsListView = new ListView<>();
        currentSubscriptionsListView.setPrefHeight(100); // Set a reasonable preferred height
        VBox.setVgrow(currentSubscriptionsListView, Priority.ALWAYS);

        subscriptionMessageLabel = new Label("");
        subscriptionMessageLabel.setWrapText(true);
        subscriptionMessageLabel.setStyle("-fx-font-weight: bold;");

        subscriptionContent.getChildren().addAll(subscribeHeader, farmerSubscriptionListView, subscribeButton,
                currentSubscriptionsHeader, currentSubscriptionsListView, subscriptionMessageLabel);
        subscriptionPane.setContent(subscriptionContent);

            view.setContent(new VBox(20, headerBox, productBrowsingPane, orderHistoryPane, subscriptionPane));

        // Set VGrow for the TitledPanes within the main VBox
        VBox.setVgrow(productBrowsingPane, Priority.ALWAYS);
        VBox.setVgrow(orderHistoryPane, Priority.ALWAYS);
        VBox.setVgrow(subscriptionPane, Priority.ALWAYS);
    }

    /**
     * Refreshes the product list view with the given list of products.
     *
     * @param products The list of products to display.
     */
    private void refreshProductList(List<Product> products) {
        productListView.getItems().clear();
        for (Product product : products) {
            productListView.getItems().add(product);
        }
    }

    /**
     * Handles adding a selected product to the customer's shopping cart.
     */
    private void handleAddToCart() {
        Product selectedProduct = productListView.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            productMessageLabel.setText("Please select a product to add to cart.");
            productMessageLabel.setTextFill(Color.RED);
            return;
        }

        // Prompt for quantity
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Add to Cart");
        dialog.setHeaderText("Enter Quantity for " + selectedProduct.getName());
        dialog.setContentText("Quantity:");

        // Correctly handle Optional<String> returned by TextInputDialog.showAndWait()
        // No explicit Optional variable, but directly using orElse on the return value
        String qtyStr = dialog.showAndWait().orElse(null); // This will be null if dialog is cancelled/closed

        if (qtyStr != null) { // Check if a quantity was entered (OK clicked)
            try {
                int quantity = Integer.parseInt(qtyStr);
                if (quantity <= 0) {
                    productMessageLabel.setText("Quantity must be positive.");
                    productMessageLabel.setTextFill(Color.RED);
                    return;
                }
                if (selectedProduct.getQuantityAvailable() < quantity) {
                    productMessageLabel.setText("Not enough stock for " + selectedProduct.getName() + ". Available: " + selectedProduct.getQuantityAvailable());
                    productMessageLabel.setTextFill(Color.RED);
                    return;
                }
                currentCustomer.addToCart(selectedProduct, quantity);
                productMessageLabel.setText(quantity + " of " + selectedProduct.getName() + " added to cart.");
                productMessageLabel.setTextFill(Color.GREEN);
                refreshShoppingCart();
            } catch (NumberFormatException e) {
                productMessageLabel.setText("Invalid quantity. Please enter a number.");
                productMessageLabel.setTextFill(Color.RED);
            } catch (ValidationException e) {
                productMessageLabel.setText("Error adding to cart: " + e.getMessage());
                productMessageLabel.setTextFill(Color.RED);
            }
        }
    }

    /**
     * Handles removing a selected item from the shopping cart.
     */
    private void handleRemoveFromCart() {
        String selectedCartItemString = shoppingCartView.getSelectionModel().getSelectedItem();
        if (selectedCartItemString == null) {
            productMessageLabel.setText("Please select an item in the cart to remove.");
            productMessageLabel.setTextFill(Color.RED);
            return;
        }

        // Parse product ID from the cart item string (e.g., "Product Name (Qty: X) - ID: P001")
        String productId = selectedCartItemString.substring(selectedCartItemString.lastIndexOf("ID: ") + 4);
        Product productToRemove = deliverySystem.getProduct(productId);

        if (productToRemove != null) {
            currentCustomer.removeFromCart(productToRemove);
            productMessageLabel.setText(productToRemove.getName() + " removed from cart.");
            productMessageLabel.setTextFill(Color.GREEN);
            refreshShoppingCart();
        } else {
            productMessageLabel.setText("Error: Could not find product in cart.");
            productMessageLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Handles clearing the entire shopping cart.
     */
    private void handleClearCart() {
        currentCustomer.clearCart();
        productMessageLabel.setText("Shopping cart cleared.");
        productMessageLabel.setTextFill(Color.GREEN);
        refreshShoppingCart();
    }

    /**
     * Refreshes the shopping cart view and updates the total.
     */
    private void refreshShoppingCart() {
        shoppingCartView.getItems().clear();
        double total = 0;
        if (currentCustomer != null) {
            for (ShoppingCartItem entry : currentCustomer.getShoppingCart()) { // Iterate ShoppingCartItem
                Product product = entry.getProduct();
                int quantity = entry.getQuantity();
                shoppingCartView.getItems().add(String.format("%s (Qty: %d) - ID: %s", product.getName(), quantity, product.getProductId()));
                total += product.getPrice() * quantity;
            }
        }
        cartTotalLabel.setText(String.format("Cart Total: $%.2f", total));
    }

    /**
     * Handles placing an order.
     * Calls the DeliverySystem to process the order and handles exceptions.
     */
    private void handlePlaceOrder() {
        if (currentCustomer.getShoppingCart().isEmpty()) {
            productMessageLabel.setText("Your shopping cart is empty.");
            productMessageLabel.setTextFill(Color.RED);
            return;
        }

        try {
            // Pass the List<ShoppingCartItem> directly
            boolean success = deliverySystem.processOrder(currentCustomer, currentCustomer.getShoppingCart());
            if (success) {
                productMessageLabel.setText("Order placed successfully! Delivery is being arranged.");
                productMessageLabel.setTextFill(Color.GREEN);
                refreshShoppingCart(); // Cart should be cleared by processOrder
                refreshProductList(deliverySystem.getAllProducts()); // Refresh product quantities
                refreshOrderHistoryTable(); // Refresh order history
            } else {
                productMessageLabel.setText("Order failed. Please check product availability.");
                productMessageLabel.setTextFill(Color.RED);
            }
        } catch (OutOfSeasonException e) {
            productMessageLabel.setText("Order failed: " + e.getMessage());
            productMessageLabel.setTextFill(Color.RED);
        } catch (DeliveryUnavailableException e) {
            productMessageLabel.setText("Order failed: " + e.getMessage());
            productMessageLabel.setTextFill(Color.RED);
        } catch (Exception e) {
            productMessageLabel.setText("An unexpected error occurred: " + e.getMessage());
            productMessageLabel.setTextFill(Color.RED);
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the order history table for the current customer.
     */
    private void refreshOrderHistoryTable() {
        if (currentCustomer != null) {
            List<Order> orders = deliverySystem.getOrdersForCustomer(currentCustomer.getUserId());
            orderHistoryTable.getItems().setAll(orders);
            if (orders.isEmpty()) {
                orderHistoryMessageLabel.setText("No past orders found.");
                orderHistoryMessageLabel.setTextFill(Color.BLACK);
            } else {
                orderHistoryMessageLabel.setText("");
            }
        }
    }

    /**
     * Handles canceling a selected order from the order history.
     */
    private void handleCancelOrder() {
        Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            orderHistoryMessageLabel.setText("Please select an order to cancel.");
            orderHistoryMessageLabel.setTextFill(Color.RED);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Order Cancellation");
        alert.setHeaderText("Cancel Order: " + selectedOrder.getOrderId());
        alert.setContentText("Are you sure you want to cancel this order? Stock will be returned.");

        // Replace Optional with direct check on ButtonType
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result == ButtonType.OK) {
            if (deliverySystem.cancelOrder(selectedOrder.getOrderId())) {
                orderHistoryMessageLabel.setText("Order " + selectedOrder.getOrderId() + " cancelled successfully.");
                orderHistoryMessageLabel.setTextFill(Color.GREEN);
                refreshOrderHistoryTable(); // Update table
                refreshProductList(deliverySystem.getAllProducts()); // Update product quantities
            } else {
                orderHistoryMessageLabel.setText("Failed to cancel order " + selectedOrder.getOrderId() + ". It might already be delivered or cancelled.");
                orderHistoryMessageLabel.setTextFill(Color.RED);
            }
        }
    }

    /**
     * Populates the list of farmers available for subscription.
     */
    private void refreshFarmerSubscriptionList() {
        farmerSubscriptionListView.getItems().clear();
        for (Farmer farmer : deliverySystem.getAllFarmers()) { // Iterate List
            farmerSubscriptionListView.getItems().add(farmer);
        }
    }

    /**
     * Handles subscribing to a selected farmer.
     */
    private void handleSubscribeToFarmer() {
        Farmer selectedFarmer = farmerSubscriptionListView.getSelectionModel().getSelectedItem();
        if (selectedFarmer == null) {
            subscriptionMessageLabel.setText("Please select a farmer to subscribe to.");
            subscriptionMessageLabel.setTextFill(Color.RED);
            return;
        }

        // For simplicity, assume a fixed subscription type
        String subscriptionType = "Weekly Produce Box";

        if (deliverySystem.subscribeToFarmer(currentCustomer.getUserId(), selectedFarmer.getUserId(), subscriptionType)) {
            subscriptionMessageLabel.setText("Successfully subscribed to " + selectedFarmer.getName() + " for " + subscriptionType + "!");
            subscriptionMessageLabel.setTextFill(Color.GREEN);
            refreshCurrentSubscriptionsList();
        } else {
            subscriptionMessageLabel.setText("Failed to subscribe to " + selectedFarmer.getName() + ". You might already be subscribed.");
            subscriptionMessageLabel.setTextFill(Color.RED);
        }
    }

    /**
     * Refreshes the list of current subscriptions for the customer.
     */
    private void refreshCurrentSubscriptionsList() {
        currentSubscriptionsListView.getItems().clear();
        for (Subscription sub : deliverySystem.getSubscriptionsForCustomer(currentCustomer.getUserId())) { // Iterate List
            currentSubscriptionsListView.getItems().add(sub);
        }
        if (currentSubscriptionsListView.getItems().isEmpty()) {
            subscriptionMessageLabel.setText("You have no active subscriptions.");
            subscriptionMessageLabel.setTextFill(Color.BLACK);
        } else {
            subscriptionMessageLabel.setText("");
        }
    }

    /**
     * Returns the VBox containing the entire Customer Portal GUI.
     *
     * @return The VBox view.
     */
    public ScrollPane getView() {
        return view;
    }
}