package com.onlinemarketplace.service;

import com.onlinemarketplace.exception.DeliveryUnavailableException;
import com.onlinemarketplace.exception.OutOfSeasonException;
import com.onlinemarketplace.exception.ValidationException;
import com.onlinemarketplace.exception.HarvestDateException;
import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.Order;
import com.onlinemarketplace.model.OrderItem;
import com.onlinemarketplace.model.Product;
import com.onlinemarketplace.model.ShoppingCartItem;
import com.onlinemarketplace.model.Subscription;
import com.onlinemarketplace.model.User;
import com.onlinemarketplace.util.FileHandler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
// Removed: ConcurrentHashMap, ExecutorService, Executors, TimeUnit, ReentrantLock, UUID, Map, Optional, Collectors

/**
 * The core system for the Online Marketplace.
 * Manages inventory, customers, farmers, processes orders, handles subscriptions,
 * and integrates with file I/O for persistence.
 * Implements basic concurrency with synchronized blocks and direct Thread management.
 */
public class DeliverySystem {
    // Using ArrayLists for data storage, manual lookup by ID
    private final List<Product> products;
    private final List<Customer> customers;
    private final List<Farmer> farmers;
    private final List<Order> orders;
    private final List<Subscription> subscriptions;
    private final List<User> users;

    // Object for intrinsic lock for thread-safe inventory updates
    private final Object inventoryLock = new Object();

    // For simple ID generation
    private int nextUserId = 1;
    private int nextProductId = 1;
    private int nextOrderId = 1;
    private int nextSubscriptionId = 1;

    // Product search engine instance
    private final ProductSearchEngine productSearchEngine;

    /**
     * Constructs a new DeliverySystem.
     * Initializes data structures and loads data from files.
     */
    public DeliverySystem() {
        this.products = new ArrayList<>();
        this.customers = new ArrayList<>();
        this.farmers = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.subscriptions = new ArrayList<>();
        this.users = new ArrayList<>();
        this.productSearchEngine = new ProductSearchEngine(this);
        loadData(); // Load data on system initialization
        addInitialSampleDataIfEmpty(); // Add sample data if files were empty
    }

    /**
     * Adds initial sample data if the system's data collections are empty.
     * This helps in testing without manually creating users/products every time.
     */
    private void addInitialSampleDataIfEmpty() {
        if (users.isEmpty()) {
            System.out.println("Adding initial sample users...");
            try {
                // Add default Farmer
                Farmer defaultFarmer = new Farmer(generateNewUserId("F"), "Green Acres Farm", "greenacres@example.com", "farmerpass", "123 Farm Rd, Ruralville");
                registerUser(defaultFarmer);

                // Add default Customer
                Customer defaultCustomer = new Customer(generateNewUserId("C"), "Alice Smith", "alice@example.com", "customerpass", "456 Oak Ave, Cityville");
                registerUser(defaultCustomer);

                // Add some initial products for the default farmer
                Product p1 = new Product(generateNewProductId(), "Organic Carrots", "Fresh, seasonal, organic", 2.50, 100, LocalDate.now().minusDays(5), defaultFarmer.getUserId());
                addProduct(p1);
                Product p2 = new Product(generateNewProductId(), "Farm Eggs", "Free-range, large dozen", 4.00, 50, LocalDate.now().minusDays(2), "F001");
                addProduct(p2);
                Product p3 = new Product(generateNewProductId(), "Heirloom Tomatoes", "Seasonal, juicy", 3.75, 75, LocalDate.now().minusDays(10), "F001");
                addProduct(p3);
                Product p4 = new Product(generateNewProductId(), "Blueberries", "Sweet, in season", 5.50, 60, LocalDate.now().minusDays(3), "F001");
                addProduct(p4);

                System.out.println("Initial sample data added.");
            } catch (ValidationException | HarvestDateException e) {
                System.err.println("Error adding initial sample data: " + e.getMessage());
            }
            saveData(); // Save the newly added sample data
        }
    }

    // --- Basic ID Generation ---
    private String generateNewUserId(String prefix) {
        String newId;
        do {
            newId = prefix + String.format("%03d", nextUserId++);
        } while (getUser(newId) != null); // Ensure uniqueness
        return newId;
    }

    private String generateNewProductId() {
        String newId;
        do {
            newId = "P" + String.format("%03d", nextProductId++);
        } while (getProduct(newId) != null); // Ensure uniqueness
        return newId;
    }

    private String generateNewOrderId() {
        String newId;
        do {
            newId = "ORD-" + String.format("%04d", nextOrderId++);
        } while (getOrder(newId) != null); // Ensure uniqueness
        return newId;
    }

    private String generateNewSubscriptionId() {
        String newId;
        do {
            newId = "SUB-" + String.format("%04d", nextSubscriptionId++);
        } while (getSubscription(newId) != null); // Ensure uniqueness
        return newId;
    }


    // --- User Management (Authentication & Registration) ---

    /**
     * Registers a new user (Customer or Farmer) into the system.
     *
     * @param user The User object to register.
     * @return true if registration is successful, false if user ID already exists.
     */
    public boolean registerUser(User user) {
        synchronized (users) { // Synchronize access to users list
            if (getUser(user.getUserId()) != null) { // Check if user ID already exists
                System.err.println("Registration failed: User ID " + user.getUserId() + " already exists.");
                return false;
            }
            users.add(user);
            if (user instanceof Customer) {
                customers.add((Customer) user);
            } else if (user instanceof Farmer) {
                farmers.add((Farmer) user);
            }
            System.out.println("User " + user.getUserId() + " registered successfully as " + user.getUserType());
            return true;
        }
    }

    /**
     * Authenticates a user based on their ID and password.
     *
     * @param userId The ID of the user attempting to log in.
     * @param password The password provided by the user.
     * @return The authenticated User object if successful, null otherwise.
     */
    public User authenticateUser(String userId, String password) {
        synchronized (users) { // Synchronize access to users list
            for (User user : users) {
                if (user.getUserId().equals(userId) && user.authenticate(password)) {
                    System.out.println("User " + userId + " authenticated successfully.");
                    return user;
                }
            }
            System.err.println("Authentication failed for user " + userId + ": Invalid credentials.");
            return null;
        }
    }

    /**
     * Retrieves a user by their ID.
     * @param userId The ID of the user.
     * @return The User object, or null if not found.
     */
    public User getUser(String userId) {
        synchronized (users) {
            for (User user : users) {
                if (user.getUserId().equals(userId)) {
                    return user;
                }
            }
            return null;
        }
    }

    // --- Product Management ---

    /**
     * Adds a product to the system's inventory.
     *
     * @param product The product to add.
     */
    public void addProduct(Product product) {
        synchronized (products) { // Synchronize access to products list
            if (product != null) {
                // Check if product already exists by ID and update it
                boolean found = false;
                for (int i = 0; i < products.size(); i++) {
                    if (products.get(i).getProductId().equals(product.getProductId())) {
                        products.set(i, product); // Update existing product
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    products.add(product); // Add new product
                }

                // Also update the farmer's product list
                Farmer farmer = getFarmer(product.getFarmerId());
                if (farmer != null) {
                    farmer.addOrUpdateProduct(product);
                }
            }
        }
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId The ID of the product.
     * @return The Product object, or null if not found.
     */
    public Product getProduct(String productId) {
        synchronized (products) { // Synchronize access to products list
            for (Product product : products) {
                if (product.getProductId().equals(productId)) {
                    return product;
                }
            }
            return null;
        }
    }

    /**
     * Removes a product from the system.
     *
     * @param productId The ID of the product to remove.
     * @return true if the product was removed, false otherwise.
     */
    public boolean removeProduct(String productId) {
        synchronized (inventoryLock) { // Use the common inventory lock
            Product removedProduct = null;
            for (int i = 0; i < products.size(); i++) {
                if (products.get(i).getProductId().equals(productId)) {
                    removedProduct = products.remove(i);
                    break;
                }
            }

            if (removedProduct != null) {
                // Also remove from the farmer's product list
                Farmer farmer = getFarmer(removedProduct.getFarmerId());
                if (farmer != null) {
                    farmer.removeProduct(productId);
                }
                System.out.println("Product " + productId + " removed successfully.");
                return true;
            }
            System.err.println("Product " + productId + " not found for removal.");
            return false;
        }
    }

    /**
     * Updates the quantity of a product in a thread-safe manner.
     *
     * @param productId The ID of the product to update.
     * @param quantityChange The amount to change the quantity by (positive for increase, negative for decrease).
     * @return true if the quantity was updated successfully, false otherwise (e.g., product not found, insufficient stock).
     */
    public boolean updateProductQuantity(String productId, int quantityChange) {
        synchronized (inventoryLock) { // Acquire lock for thread-safe update
            Product product = getProduct(productId); // getProduct is already synchronized on products list
            if (product == null) {
                System.err.println("Error: Product with ID " + productId + " not found.");
                return false;
            }

            int currentQuantity = product.getQuantityAvailable();
            int newQuantity = currentQuantity + quantityChange;

            if (newQuantity < 0) {
                System.err.println("Error: Insufficient stock for product " + product.getName() + ". Available: " + currentQuantity + ", Requested change: " + quantityChange);
                return false; // Cannot go below zero
            }

            try {
                product.setQuantityAvailable(newQuantity);
                System.out.println("Updated quantity for " + product.getName() + " to " + newQuantity);
                return true;
            } catch (ValidationException e) {
                System.err.println("Validation error updating quantity for " + product.getName() + ": " + e.getMessage());
                return false;
            }
        }
    }

    // --- Customer & Farmer Access ---

    public Customer getCustomer(String customerId) {
        synchronized (customers) {
            for (Customer customer : customers) {
                if (customer.getUserId().equals(customerId)) {
                    return customer;
                }
            }
            return null;
        }
    }

    public Farmer getFarmer(String farmerId) {
        synchronized (farmers) {
            for (Farmer farmer : farmers) {
                if (farmer.getUserId().equals(farmerId)) {
                    return farmer;
                }
            }
            return null;
        }
    }

    // --- Order Processing ---

    /**
     * Processes a customer's order.
     * This method handles inventory deduction and simulates order matching and delivery.
     * Creates and stores a new Order object.
     *
     * @param customer The customer placing the order.
     * @param orderItems List of ShoppingCartItem (Product and quantity) from customer's cart.
     * @return true if the order was processed successfully, false otherwise.
     * @throws OutOfSeasonException If any ordered item is out of season.
     * @throws DeliveryUnavailableException If delivery is not available for the customer's address.
     */
    public boolean processOrder(Customer customer, List<ShoppingCartItem> orderItems)
            throws OutOfSeasonException, DeliveryUnavailableException {
        if (customer == null || orderItems == null || orderItems.isEmpty()) {
            System.err.println("Error: Invalid customer or empty order items.");
            return false;
        }

        // 1. Pre-check for seasonality and delivery availability
        LocalDate today = LocalDate.now();
        double calculatedTotal = 0;
        List<OrderItem> orderItemsForOrderObject = new ArrayList<>();

        for (ShoppingCartItem cartItem : orderItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getQuantity();

            if (!product.isInSeason(today)) {
                throw new OutOfSeasonException("Product " + product.getName() + " is currently out of season.");
            }
            calculatedTotal += product.getPrice() * quantity;
            orderItemsForOrderObject.add(new OrderItem(product.getProductId(), quantity));
        }

        if (!customer.checkServiceEligibility()) {
            throw new DeliveryUnavailableException("Delivery is not available for your address: " + customer.getDeliveryAddress());
        }

        // 2. Check and reserve inventory (thread-safe)
        synchronized (inventoryLock) { // Use the common inventory lock
            // First, check if all items are available
            for (ShoppingCartItem cartItem : orderItems) {
                Product orderedProduct = getProduct(cartItem.getProduct().getProductId());
                int orderedQuantity = cartItem.getQuantity();

                if (orderedProduct == null || orderedProduct.getQuantityAvailable() < orderedQuantity) {
                    System.err.println("Error: Insufficient stock for " + cartItem.getProduct().getName() + ". Available: " +
                            (orderedProduct != null ? orderedProduct.getQuantityAvailable() : "0") + ", Requested: " + orderedQuantity);
                    return false;
                }
            }

            // If all are available, deduct quantities
            for (ShoppingCartItem cartItem : orderItems) {
                Product orderedProduct = getProduct(cartItem.getProduct().getProductId());
                int orderedQuantity = cartItem.getQuantity();
                updateProductQuantity(orderedProduct.getProductId(), -orderedQuantity); // Deduct quantity
            }

            // 3. Create and store the Order object
            String orderId = generateNewOrderId();
            Order newOrder = new Order(orderId, customer.getUserId(), LocalDate.now(), orderItemsForOrderObject, calculatedTotal, "Pending");
            synchronized (orders) { // Synchronize access to orders list
                orders.add(newOrder);
            }
            System.out.println("Order " + orderId + " for Customer " + customer.getName() + " placed successfully.");

            // 4. Simulate delivery route calculation concurrently
            final String finalOrderId = orderId; // Need final variable for inner class
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Calculating delivery route for order " + finalOrderId + " to " + customer.getDeliveryAddress() + "...");
                        Thread.sleep(3000); // Simulate network latency or complex calculation
                        // Only set status to Dispatched if it's still Pending (not cancelled by customer)
                        Order currentOrder = getOrder(finalOrderId); // getOrder is synchronized
                        if (currentOrder != null && "Pending".equals(currentOrder.getStatus())) {
                           currentOrder.setStatus("Dispatched"); // Update order status
                        }
                        System.out.println("Delivery route calculated for order " + finalOrderId + ". Dispatching...");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        System.err.println("Delivery route calculation interrupted for order " + finalOrderId);
                    }
                }
            }).start();


            customer.clearCart(); // Clear customer's cart after successful order
            return true;
        }
    }

    /**
     * Retrieves an order by its ID.
     * @param orderId The ID of the order.
     * @return The Order object, or null if not found.
     */
    public Order getOrder(String orderId) {
        synchronized (orders) {
            for (Order order : orders) {
                if (order.getOrderId().equals(orderId)) {
                    return order;
                }
            }
            return null;
        }
    }

    /**
     * Cancels an existing order.
     * @param orderId The ID of the order to cancel.
     * @return true if the order was cancelled, false otherwise.
     */
    public boolean cancelOrder(String orderId) {
        Order orderToCancel = getOrder(orderId); // getOrder is synchronized
        if (orderToCancel == null) {
            System.err.println("Order " + orderId + " not found for cancellation.");
            return false;
        }
        if ("Cancelled".equals(orderToCancel.getStatus()) || "Delivered".equals(orderToCancel.getStatus())) {
            System.err.println("Order " + orderId + " cannot be cancelled as it is already " + orderToCancel.getStatus() + ".");
            return false;
        }

        // Simulate refunding stock
        synchronized (inventoryLock) { // Use the common inventory lock
            for (OrderItem item : orderToCancel.getOrderedItems()) {
                String productId = item.getProductId();
                int quantity = item.getQuantity();
                updateProductQuantity(productId, quantity); // Add stock back (updateProductQuantity is synchronized)
            }
            orderToCancel.setStatus("Cancelled");
            System.out.println("Order " + orderId + " cancelled successfully. Stock refunded.");
            return true;
        }
    }

    /**
     * Retrieves orders placed by a specific customer.
     * @param customerId The ID of the customer.
     * @return A list of orders by the customer.
     */
    public List<Order> getOrdersForCustomer(String customerId) {
        List<Order> customerOrders = new ArrayList<>();
        synchronized (orders) {
            for (Order order : orders) {
                if (customerId == null || order.getCustomerId().equals(customerId)) { // If customerId is null, return all orders
                    customerOrders.add(order);
                }
            }
        }
        // Basic Bubble Sort by Order Date (descending)
        for (int i = 0; i < customerOrders.size() - 1; i++) {
            for (int j = 0; j < customerOrders.size() - i - 1; j++) {
                if (customerOrders.get(j).getOrderDate().isBefore(customerOrders.get(j + 1).getOrderDate())) {
                    // Swap
                    Order temp = customerOrders.get(j);
                    customerOrders.set(j, customerOrders.get(j + 1));
                    customerOrders.set(j + 1, temp);
                }
            }
        }
        return customerOrders;
    }

    /**
     * Retrieves orders that contain products from a specific farmer.
     * This is a simplified approach; a real system might link orders directly to farmers.
     *
     * @param farmerId The ID of the farmer.
     * @return A list of orders relevant to the farmer.
     */
    public List<Order> getOrdersForFarmer(String farmerId) {
        List<Order> farmerOrders = new ArrayList<>();
        synchronized (orders) {
            for (Order order : orders) {
                boolean containsFarmerProduct = false;
                for (OrderItem item : order.getOrderedItems()) {
                    Product product = getProduct(item.getProductId()); // getProduct is synchronized
                    if (product != null && product.getFarmerId().equals(farmerId)) {
                        containsFarmerProduct = true;
                        break;
                    }
                }
                if (containsFarmerProduct) {
                    farmerOrders.add(order);
                }
            }
        }
        // Basic Bubble Sort by Order Date (descending)
        for (int i = 0; i < farmerOrders.size() - 1; i++) {
            for (int j = 0; j < farmerOrders.size() - i - 1; j++) {
                if (farmerOrders.get(j).getOrderDate().isBefore(farmerOrders.get(j + 1).getOrderDate())) {
                    // Swap
                    Order temp = farmerOrders.get(j);
                    farmerOrders.set(j, farmerOrders.get(j + 1));
                    farmerOrders.set(j + 1, temp);
                }
            }
        }
        return farmerOrders;
    }

    // --- Subscription Management ---

    /**
     * Allows a customer to subscribe to a farmer.
     *
     * @param customerId The ID of the subscribing customer.
     * @param farmerId The ID of the farmer to subscribe to.
     * @param subscriptionType The type of subscription.
     * @return true if subscription is successful, false if already subscribed or invalid IDs.
     */
    public boolean subscribeToFarmer(String customerId, String farmerId, String subscriptionType) {
        if (getCustomer(customerId) == null || getFarmer(farmerId) == null) { // getCustomer/Farmer are synchronized
            System.err.println("Subscription failed: Invalid customer or farmer ID.");
            return false;
        }

        synchronized (subscriptions) { // Synchronize access to subscriptions list
            // Check if already actively subscribed
            boolean alreadySubscribed = false;
            for (Subscription s : subscriptions) {
                if (s.getCustomerId().equals(customerId) && s.getFarmerId().equals(farmerId) && s.getStatus().equals("Active")) {
                    alreadySubscribed = true;
                    break;
                }
            }

            if (alreadySubscribed) {
                System.out.println("Customer " + customerId + " is already actively subscribed to farmer " + farmerId);
                return false;
            }

            String subscriptionId = generateNewSubscriptionId();
            Subscription newSubscription = new Subscription(subscriptionId, customerId, farmerId, LocalDate.now(), "Active", subscriptionType);
            subscriptions.add(newSubscription);
            System.out.println("Customer " + customerId + " subscribed to Farmer " + farmerId + " for " + subscriptionType);
            return true;
        }
    }

    /**
     * Retrieves a subscription by its ID.
     * @param subscriptionId The ID of the subscription.
     * @return The Subscription object, or null if not found.
     */
    public Subscription getSubscription(String subscriptionId) {
        synchronized (subscriptions) {
            for (Subscription sub : subscriptions) {
                if (sub.getSubscriptionId().equals(subscriptionId)) {
                    return sub;
                }
            }
            return null;
        }
    }

    /**
     * Retrieves all active subscriptions for a given customer.
     * @param customerId The ID of the customer.
     * @return A list of active subscriptions for the customer.
     */
    public List<Subscription> getSubscriptionsForCustomer(String customerId) {
        List<Subscription> customerSubscriptions = new ArrayList<>();
        synchronized (subscriptions) {
            for (Subscription s : subscriptions) {
                if (s.getCustomerId().equals(customerId) && s.getStatus().equals("Active")) {
                    customerSubscriptions.add(s);
                }
            }
        }
        return customerSubscriptions;
    }

    // --- Data Access (return copies to prevent external modification) ---

    public List<Product> getAllProducts() {
        synchronized (products) {
            return new ArrayList<>(products); // Return a copy
        }
    }

    public List<Customer> getAllCustomers() {
        synchronized (customers) {
            return new ArrayList<>(customers); // Return a copy
        }
    }

    public List<Farmer> getAllFarmers() {
        synchronized (farmers) {
            return new ArrayList<>(farmers); // Return a copy
        }
    }

    public List<User> getAllUsers() {
        synchronized (users) {
            return new ArrayList<>(users); // Return a copy
        }
    }

    public IProductSearch getProductSearchEngine() {
        return productSearchEngine;
    }

    // --- File I/O ---

    /**
     * Loads all data (products, customers, farmers, orders, subscriptions) from their respective text files.
     */
    public void loadData() {
        System.out.println("Loading data...");
        try {
            // Load Users (Customers and Farmers are also added to the users list)
            List<User> loadedUsers = FileHandler.loadUsers("users.txt");
            synchronized (users) {
                for (User user : loadedUsers) {
                    users.add(user);
                    if (user instanceof Customer) {
                        customers.add((Customer) user);
                    } else if (user instanceof Farmer) {
                        farmers.add((Farmer) user);
                    }
                    // Update ID counters based on loaded data
                    String idNumStr = user.getUserId().substring(1); // "C001" -> "001"
                    try {
                        int idNum = Integer.parseInt(idNumStr);
                        if (idNum >= nextUserId) {
                            nextUserId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore if ID is not purely numeric after prefix
                    }
                }
            }
            System.out.println("Loaded " + users.size() + " users.");

            // Load Products
            List<Product> loadedProducts = FileHandler.loadProducts("produce.txt");
            synchronized (products) {
                for (Product p : loadedProducts) {
                    products.add(p);
                    // Update ID counters
                    String idNumStr = p.getProductId().substring(1);
                    try {
                        int idNum = Integer.parseInt(idNumStr);
                        if (idNum >= nextProductId) {
                            nextProductId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {}
                }
            }
            System.out.println("Loaded " + products.size() + " products.");

            // After loading, ensure products are correctly linked to farmers
            // This loop iterates over a copy of products to avoid ConcurrentModificationException
            List<Product> productsCopy = new ArrayList<>(products);
            for (Product product : productsCopy) {
                Farmer farmer = getFarmer(product.getFarmerId());
                if (farmer != null) {
                    farmer.addOrUpdateProduct(product); // This will update the farmer's internal list
                } else {
                    System.err.println("Warning: Product " + product.getName() + " (ID: " + product.getProductId() + ") references non-existent farmer ID: " + product.getFarmerId());
                }
            }

            // Load Orders
            List<Order> loadedOrders = FileHandler.loadOrders("orders.txt");
            synchronized (orders) {
                for (Order o : loadedOrders) {
                    orders.add(o);
                    // Update ID counters
                    String idNumStr = o.getOrderId().substring(o.getOrderId().indexOf("-") + 1);
                    try {
                        int idNum = Integer.parseInt(idNumStr);
                        if (idNum >= nextOrderId) {
                            nextOrderId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {}
                }
            }
            System.out.println("Loaded " + orders.size() + " orders.");

            // Load Subscriptions
            List<Subscription> loadedSubscriptions = FileHandler.loadSubscriptions("subscriptions.txt");
            synchronized (subscriptions) {
                for (Subscription s : loadedSubscriptions) {
                    subscriptions.add(s);
                    // Update ID counters
                    String idNumStr = s.getSubscriptionId().substring(s.getSubscriptionId().indexOf("-") + 1);
                    try {
                        int idNum = Integer.parseInt(idNumStr);
                        if (idNum >= nextSubscriptionId) {
                            nextSubscriptionId = idNum + 1;
                        }
                    } catch (NumberFormatException e) {}
                }
            }
            System.out.println("Loaded " + subscriptions.size() + " subscriptions.");

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Saves all current data (products, customers, farmers, orders, subscriptions) to their respective text files.
     */
    public void saveData() {
        System.out.println("Saving data...");
        try {
            FileHandler.saveUsers("users.txt", new ArrayList<>(users)); // Pass copy
            FileHandler.saveProducts("produce.txt", new ArrayList<>(products)); // Pass copy
            FileHandler.saveOrders("orders.txt", new ArrayList<>(orders)); // Pass copy
            FileHandler.saveSubscriptions("subscriptions.txt", new ArrayList<>(subscriptions)); // Pass copy
            System.out.println("Data saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Shuts down the system (no complex executor service to shut down in this basic version).
     */
    public void shutdown() {
        // In this basic version, there's no explicit executor service to shut down.
        // Threads created for delivery simulation will eventually complete.
        System.out.println("DeliverySystem shutdown (no active threads to terminate).");
    }
}