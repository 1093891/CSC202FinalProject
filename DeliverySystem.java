
package marketplace;

import java.util.*;

public class DeliverySystem {
    private List<Farmer> farmers;
    private List<Customer> customers;
    
    public DeliverySystem() {
        farmers = new ArrayList<>();
        customers= new ArrayList<>();
    }
    
    //Getters
    public List<Farmer> getFarmers() {
        return farmers;
    }
    public List<Customer> getCustomers() {
        return customers;
    }
    
    public void registerFarmer(Farmer farmer) {
        farmers.add(farmer);
    }
    
    public void registerCustomer(Customer customer) {
        customers.add(customer);
    }
    
    public void listAvailableProducts() {
        System.out.println("Available Products: ");
        for (Farmer f: farmers) {
            for (Product p: f.getInventory()) {
                System.out.println(f.getName() + ": " +p);
            }
        }        
    }
    
    public boolean processOrder(String customerId, String productId, int requestedQuantity) {
        Customer c=findCustomerById(customerId);
        if (c==null) {
            System.out.println("Customer not found.");
            return false;
        }
        for (Farmer f:farmers) {
            for (Product p: f.getInventory()) {
                if (p.getProductId().equals(productId) && requestedQuantity<= p.getQuantity()){
                    Product ordered= new Product(p.getProductId(), p.getName(), p.getDescription(), p.getPrice(), requestedQuantity, p.getHarvestDate());
                    c.addToCart(ordered);
                    p.setQuantity(p.getQuantity()-requestedQuantity);
                    System.out.println("Order placed successfully for " + requestedQuantity + "x " + p.getName() +".");
                    return true;
                }
            }
        }
        System.out.println("Product Unavailable or insufficient quantity.");
        return false;
    }
    
    public Customer findCustomerById(String customerId) {
        for (Customer c: customers) {
            if (c.getId().equals(customerId))
                return c; 
        }
        return null;
    }
}
