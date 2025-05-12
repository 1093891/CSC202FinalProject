package marketplace;

import java.time.LocalDate;

public class Product {
    private String productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private LocalDate harvestDate;
    
    public Product(String productId, String name, String description, double price, int quantity, LocalDate harvestDate) {
        this.productId=productId;
        this.name= name;
        this.description=description;
        setPrice(price);
        setQuantity(quantity);
        setHarvestDate(harvestDate);
    }
    
    //Getters
    public String getProductId() {return productId;}
    public String getName() {return name;}
    public String getDescription() {return description;}
    public double getPrice() {return price;}
    public int getQuantity() {return quantity;}
    public LocalDate getHarvestDate() {return harvestDate;}
    
    //Setters
    public void setPrice(double price) {
        if (price<=0) {
            throw new IllegalArgumentException("price Must be positive");
        }
        this.price= price;
    }
    public void setQuantity(int quantity) {
        if (quantity<0){
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity=quantity;
    }
    public void setHarvestDate(LocalDate harvestDate) {
        if (harvestDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Harvest date must be in the future.");
        }
        this.harvestDate= harvestDate;
    }
    
     @Override
    public String toString() {
        return name + " [" + description + "] - $" + price +
               " | Qty: " + quantity +
               " | Harvest: " + harvestDate;
    }
}
