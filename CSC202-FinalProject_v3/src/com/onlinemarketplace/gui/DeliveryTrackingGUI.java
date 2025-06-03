package com.onlinemarketplace.gui;

import com.onlinemarketplace.model.Customer;
import com.onlinemarketplace.model.Order;
import com.onlinemarketplace.service.DeliverySystem;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GUI component for Delivery Tracking.
 * Simulates a delivery tracking map and displays active orders.
 * Uses a simple animation for delivery vehicle movement.
 */
public class DeliveryTrackingGUI {

    private final DeliverySystem deliverySystem;
    private VBox view;
    private Pane mapPane; // Using Pane for simple shapes
    private Label activeOrdersLabel;

    // Simulated delivery data
    private List<DeliveryStatus> simulatedDeliveries;
    private Random random;

    /**
     * Represents the simulated status of a delivery.
     */
    private static class DeliveryStatus {
        String orderId;
        String customerId;
        String customerAddress;
        String status; // e.g., "Processing", "Dispatched", "In Transit", "Delivered"
        double vehicleX, vehicleY; // Current position on map
        double targetX, targetY; // Destination on map
        Circle vehicleIcon; // Visual representation of the vehicle
        Timeline deliveryTimeline; // Timeline for animation

        DeliveryStatus(String orderId, String customerId, String customerAddress, double startX, double startY, double targetX, double targetY) {
            this.orderId = orderId;
            this.customerId = customerId;
            this.customerAddress = customerAddress;
            this.status = "Processing"; // Initial status for simulation
            this.vehicleX = startX;
            this.vehicleY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.vehicleIcon = new Circle(startX, startY, 8, Color.BLUE); // Blue circle for vehicle
            this.vehicleIcon.setStroke(Color.DARKBLUE);
            this.vehicleIcon.setStrokeWidth(2);
        }

        public String getStatusText() {
            return String.format(
                    "Order ID: %s, Customer: %s, Status: %s",
                    orderId, customerAddress, status
            );
        }
    }

    public DeliveryTrackingGUI(DeliverySystem deliverySystem) {
        this.deliverySystem = deliverySystem;
        this.simulatedDeliveries = new ArrayList<>();
        this.random = new Random();
        initializeGUI();
        startSimulationUpdates();
    }

    // GUI initialization (remains unchanged)
    private void initializeGUI() {
        view = new VBox(10);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: #f0f8ff;"); // Alice blue background

        Label title = new Label("Delivery Tracking Map");
        title.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0056b3;"
        ); // Darker blue

        activeOrdersLabel = new Label("Active Deliveries: \nNone");
        activeOrdersLabel.setWrapText(true);
        activeOrdersLabel.setStyle(
                "-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;"
        );

        mapPane = new Pane();
        mapPane.setStyle(
                "-fx-background-color: #ADD8E6; -fx-border-color: #333; -fx-border-width: 2px; -fx-border-radius: 5px;"
        ); // Light blue map
        mapPane.setPrefSize(800, 500); // Fixed size for the map area

        view.getChildren().addAll(title, activeOrdersLabel, mapPane);
    }

    private void startSimulationUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
            updateSimulatedDeliveries();
            updateActiveOrdersLabel();
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void updateSimulatedDeliveries() {
        // Fetch dispatched orders
        List<Order> systemDispatchedOrders = deliverySystem
                .getOrdersForCustomer(null)
                .stream()
                .filter(order -> "Dispatched".equals(order.getStatus()))
                .toList();

        // Add new deliveries
        for (Order order : systemDispatchedOrders) {
            boolean alreadySimulated = simulatedDeliveries.stream()
                    .anyMatch(delivery -> delivery.orderId.equals(order.getOrderId()));
            if (!alreadySimulated) {
                Customer customer = deliverySystem.getCustomer(order.getCustomerId());
                if (customer != null) {
                    double startX = random.nextDouble() * mapPane.getWidth();
                    double startY = random.nextDouble() * mapPane.getHeight();
                    double targetX = random.nextDouble() * mapPane.getWidth();
                    double targetY = random.nextDouble() * mapPane.getHeight();

                    DeliveryStatus newDelivery = new DeliveryStatus(
                            order.getOrderId(),
                            customer.getUserId(),
                            customer.getDeliveryAddress(),
                            startX,
                            startY,
                            targetX,
                            targetY
                    );
                    newDelivery.status = "In Transit"; // Set to "In Transit"
                    simulatedDeliveries.add(newDelivery);
                    mapPane.getChildren().add(newDelivery.vehicleIcon);

                    // Start animation
                    newDelivery.deliveryTimeline = new Timeline(
                            new KeyFrame(
                                    Duration.millis(50),
                                    e -> moveVehicle(newDelivery)
                            )
                    );
                    newDelivery.deliveryTimeline.setCycleCount(
                            Animation.INDEFINITE
                    );
                    newDelivery.deliveryTimeline.play();
                }
            }
        }

        // Update deliveries (remove completed/canceled ones)
        simulatedDeliveries.removeIf(delivery -> {
            Order actualOrder = deliverySystem.getOrder(delivery.orderId);

            if (actualOrder != null && "Cancelled".equals(actualOrder.getStatus())) {
                if (delivery.deliveryTimeline != null)
                    delivery.deliveryTimeline.stop();
                mapPane.getChildren().remove(delivery.vehicleIcon);
                return true;
            }

            if (
                    Math.abs(delivery.vehicleX - delivery.targetX) < 5 &&
                            Math.abs(delivery.vehicleY - delivery.targetY) < 5
            ) {
                delivery.status = "Delivered";
                if (actualOrder != null) actualOrder.setStatus("Delivered");
                if (delivery.deliveryTimeline != null)
                    delivery.deliveryTimeline.stop();
                mapPane.getChildren().remove(delivery.vehicleIcon);
                return true;
            }
            return false;
        });
    }

    private void moveVehicle(DeliveryStatus delivery) {
        double speed = 2.0;
        double dx = delivery.targetX - delivery.vehicleX;
        double dy = delivery.targetY - delivery.vehicleY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > speed) {
            delivery.vehicleX += speed * (dx / distance);
            delivery.vehicleY += speed * (dy / distance);
        } else {
            delivery.vehicleX = delivery.targetX;
            delivery.vehicleY = delivery.targetY;
        }
        delivery.vehicleIcon.setCenterX(delivery.vehicleX);
        delivery.vehicleIcon.setCenterY(delivery.vehicleY);
    }

    private void updateActiveOrdersLabel() {
        StringBuilder sb = new StringBuilder("Active Deliveries:\n");
        if (simulatedDeliveries.isEmpty()) {
            sb.append("None");
        } else {
            simulatedDeliveries.forEach(delivery -> sb.append("- ").append(delivery.getStatusText()).append("\n"));
        }
        activeOrdersLabel.setText(sb.toString());
    }

    public VBox getView() {
        return view;
    }
}