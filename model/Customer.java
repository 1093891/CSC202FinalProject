
package marketplace;

import java.util.ArrayList;
import java.util.List;

public class Customer extends User {
    private String deliveryAddress;
    private List<Product> cart;
    
    public Customer(String id, String name, String email, String deliveryAddress) {
        super(id, name, email);
        this.deliveryAddress= deliveryAddress;
        this.cart= new ArrayList<>();
    }
    
    //Getters
    public String getDeliveryAddress() {return deliveryAddress;}
    public List<Product> getCart() {return cart;}
    
    public void addToCart(Product p) {
        cart.add(p);
    }
    
    @Override
    public boolean isInServiceArea(String location) {
        //will impliment
        return deliveryAddress.contains(location);
    }
    
    @Override
    public String toString() {
        return "Customer: " + super.toString() + " | delivery: " + deliveryAddress;
    }
}
