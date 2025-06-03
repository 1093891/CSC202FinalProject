package com.onlinemarketplace.util;

import com.onlinemarketplace.exception.HarvestDateException;
import com.onlinemarketplace.exception.ValidationException;
import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.Order;
import com.onlinemarketplace.model.OrderItem;
import com.onlinemarketplace.model.Product;
import com.onlinemarketplace.model.Subscription;
import com.onlinemarketplace.model.User;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
// Removed: Map, HashMap, Collectors

/**
 * Utility class for handling file I/O operations for persistence.
 * It provides static methods to load and save Product, Customer, Farmer, Order, and Subscription data to text files.
 * Data is stored in a simple delimited format.
 */
public class FileHandler {

    // Changed the delimiter to a less common sequence to avoid conflicts with data containing commas
    private static final String DELIMITER = "###";
    private static final String INNER_DELIMITER = "@@@"; // For map entries within Order

    // --- User (Customer/Farmer) Operations ---

    /**
     * Loads user data (both customers and farmers) from a specified file.
     * Each line represents a user.
     *
     * @param filename The name of the file to load users from.
     * @return A list of User objects (Customer or Farmer instances).
     * @throws IOException If an I/O error occurs.
     */
    public static List<User> loadUsers(String filename) throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("User file not found: " + filename + ". Creating an empty one.");
            file.createNewFile();
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                try {
                    String userType = parts[0].trim();
                    String userId = parts[1].trim();
                    String name = parts[2].trim();
                    String email = parts[3].trim();
                    String password = parts[4].trim();

                    if ("Customer".equalsIgnoreCase(userType) && parts.length == 6) {
                        String deliveryAddress = parts[5].trim();
                        users.add(new Customer(userId, name, email, password, deliveryAddress));
                    } else if ("Farmer".equalsIgnoreCase(userType) && parts.length == 6) {
                        String farmAddress = parts[5].trim();
                        users.add(new Farmer(userId, name, email, password, farmAddress));
                    } else {
                        System.err.println("Skipping malformed user line (incorrect number of parts or unknown type): " + line);
                    }
                } catch (ValidationException e) {
                    System.err.println("Skipping malformed user line: " + line + " - Error: " + e.getMessage());
                }
            }
        }
        return users;
    }

    /**
     * Saves a collection of User objects (Customers and Farmers) to a specified file.
     *
     * @param filename The name of the file to save users to.
     * @param users The collection of User objects to save.
     * @throws IOException If an I/O error occurs.
     */
    public static void saveUsers(String filename, List<User> users) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (User user : users) {
                String line;
                if (user instanceof Customer) {
                    Customer customer = (Customer) user;
                    line = customer.getUserType() + DELIMITER +
                           customer.getUserId() + DELIMITER +
                           customer.getName() + DELIMITER +
                           customer.getEmail() + DELIMITER +
                           customer.getPassword() + DELIMITER +
                           customer.getDeliveryAddress();
                } else if (user instanceof Farmer) {
                    Farmer farmer = (Farmer) user;
                    line = farmer.getUserType() + DELIMITER +
                           farmer.getUserId() + DELIMITER +
                           farmer.getName() + DELIMITER +
                           farmer.getEmail() + DELIMITER +
                           farmer.getPassword() + DELIMITER +
                           farmer.getFarmAddress();
                } else {
                    continue; // Skip unknown user types
                }
                writer.write(line);
                writer.newLine();
            }
        }
    }


    // --- Product Operations ---

    /**
     * Loads product data from a specified file.
     * Each line in the file should represent a product with delimited values:
     * Product ID###Name###Description###Price###Quantity Available###Harvest Date (YYYY-MM-DD)###Farmer ID
     *
     * @param filename The name of the file to load products from.
     * @return A list of Product objects.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Product> loadProducts(String filename) throws IOException {
        List<Product> products = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Product file not found: " + filename + ". Creating an empty one.");
            file.createNewFile();
            return products;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines
                String[] parts = line.split(DELIMITER);
                if (parts.length == 7) { // Expect 7 parts for a product
                    try {
                        String productId = parts[0].trim();
                        String name = parts[1].trim();
                        String description = parts[2].trim();
                        double price = Double.parseDouble(parts[3].trim());
                        int quantity = Integer.parseInt(parts[4].trim());
                        LocalDate harvestDate = LocalDate.parse(parts[5].trim());
                        String farmerId = parts[6].trim();

                        products.add(new Product(productId, name, description, price, quantity, harvestDate, farmerId));
                    } catch (NumberFormatException | DateTimeParseException | ValidationException | HarvestDateException e) {
                        System.err.println("Skipping malformed product line: " + line + " - Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed product line (incorrect number of parts): " + line + ". Expected 7, got " + parts.length);
                }
            }
        }
        return products;
    }

    /**
     * Saves a collection of Product objects to a specified file.
     * Each product is written as a delimited line.
     *
     * @param filename The name of the file to save products to.
     * @param products The collection of Product objects to save.
     * @throws IOException If an I/O error occurs.
     */
    public static void saveProducts(String filename, List<Product> products) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Product product : products) {
                String line = product.getProductId() + DELIMITER +
                              product.getName() + DELIMITER +
                              product.getDescription() + DELIMITER +
                              String.valueOf(product.getPrice()) + DELIMITER +
                              String.valueOf(product.getQuantityAvailable()) + DELIMITER +
                              product.getHarvestDate().toString() + DELIMITER +
                              product.getFarmerId();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // --- Order Operations ---

    /**
     * Loads order data from a specified file.
     * Order ID###Customer ID###Order Date###Product Quantities (PID=QTY@@@PID=QTY)###Total Amount###Status
     *
     * @param filename The name of the file to load orders from.
     * @return A list of Order objects.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Order> loadOrders(String filename) throws IOException {
        List<Order> orders = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Order file not found: " + filename + ". Creating an empty one.");
            file.createNewFile();
            return orders;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length == 6) { // Expect 6 parts for an order
                    try {
                        String orderId = parts[0].trim();
                        String customerId = parts[1].trim();
                        LocalDate orderDate = LocalDate.parse(parts[2].trim());
                        String productQuantitiesString = parts[3].trim();
                        double totalAmount = Double.parseDouble(parts[4].trim());
                        String status = parts[5].trim();

                        List<OrderItem> orderedItems = new ArrayList<>();
                        if (!productQuantitiesString.isEmpty()) {
                            String[] pqPairs = productQuantitiesString.split(INNER_DELIMITER);
                            for (String pair : pqPairs) {
                                String[] kv = pair.split("=");
                                if (kv.length == 2) {
                                    orderedItems.add(new OrderItem(kv[0].trim(), Integer.parseInt(kv[1].trim())));
                                }
                            }
                        }
                        orders.add(new Order(orderId, customerId, orderDate, orderedItems, totalAmount, status));
                    } catch (NumberFormatException | DateTimeParseException e) {
                        System.err.println("Skipping malformed order line: " + line + " - Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed order line (incorrect number of parts): " + line + ". Expected 6, got " + parts.length);
                }
            }
        }
        return orders;
    }

    /**
     * Saves a collection of Order objects to a specified file.
     *
     * @param filename The name of the file to save orders to.
     * @param orders The collection of Order objects to save.
     * @throws IOException If an I/O error occurs.
     */
    public static void saveOrders(String filename, List<Order> orders) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Order order : orders) {
                StringBuilder productQuantitiesStringBuilder = new StringBuilder();
                for (int i = 0; i < order.getOrderedItems().size(); i++) {
                    OrderItem item = order.getOrderedItems().get(i);
                    productQuantitiesStringBuilder.append(item.getProductId()).append("=").append(item.getQuantity());
                    if (i < order.getOrderedItems().size() - 1) {
                        productQuantitiesStringBuilder.append(INNER_DELIMITER);
                    }
                }
                String productQuantitiesString = productQuantitiesStringBuilder.toString();

                String line = order.getOrderId() + DELIMITER +
                              order.getCustomerId() + DELIMITER +
                              order.getOrderDate().toString() + DELIMITER +
                              productQuantitiesString + DELIMITER +
                              String.valueOf(order.getTotalAmount()) + DELIMITER +
                              order.getStatus();
                writer.write(line);
                writer.newLine();
            }
        }
    }

    // --- Subscription Operations ---

    /**
     * Loads subscription data from a specified file.
     * Subscription ID###Customer ID###Farmer ID###Start Date###Status###Subscription Type
     *
     * @param filename The name of the file to load subscriptions from.
     * @return A list of Subscription objects.
     * @throws IOException If an I/O error occurs.
     */
    public static List<Subscription> loadSubscriptions(String filename) throws IOException {
        List<Subscription> subscriptions = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("Subscription file not found: " + filename + ". Creating an empty one.");
            file.createNewFile();
            return subscriptions;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(DELIMITER);
                if (parts.length == 6) { // Expect 6 parts for a subscription
                    try {
                        String subscriptionId = parts[0].trim();
                        String customerId = parts[1].trim();
                        String farmerId = parts[2].trim();
                        LocalDate startDate = LocalDate.parse(parts[3].trim());
                        String status = parts[4].trim();
                        String subscriptionType = parts[5].trim();

                        subscriptions.add(new Subscription(subscriptionId, customerId, farmerId, startDate, status, subscriptionType));
                    } catch (DateTimeParseException e) {
                        System.err.println("Skipping malformed subscription line: " + line + " - Error: " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed subscription line (incorrect number of parts): " + line + ". Expected 6, got " + parts.length);
                }
            }
        }
        return subscriptions;
    }

    /**
     * Saves a collection of Subscription objects to a specified file.
     *
     * @param filename The name of the file to save subscriptions to.
     * @param subscriptions The collection of Subscription objects to save.
     * @throws IOException If an I/O error occurs.
     */
    public static void saveSubscriptions(String filename, List<Subscription> subscriptions) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Subscription subscription : subscriptions) {
                String line = subscription.getSubscriptionId() + DELIMITER +
                              subscription.getCustomerId() + DELIMITER +
                              subscription.getFarmerId() + DELIMITER +
                              subscription.getStartDate().toString() + DELIMITER +
                              subscription.getStatus() + DELIMITER +
                              subscription.getSubscriptionType();
                writer.write(line);
                writer.newLine();
            }
        }
    }
}
