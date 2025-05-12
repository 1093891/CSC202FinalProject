
package marketplace;

import java.util.ArrayList;
import java.util.List;

public class Farmer extends User {
    private String farmAddress;
    private List<Product> inventory;
    
    public Farmer(String id, String name, String email, String farmAddress) {
        super(id, name, email);
        this.farmAddress=farmAddress;
        this.inventory=new ArrayList<>();
    }
    
    //Getters
    public String getFarmAddress() {return farmAddress;}
    public List<Product> getInventory() {return inventory;}
    
    public void addProduct(Product p) {
        inventory.add(p);
    }
    
    @Override
    public boolean isInServiceArea(String location) {
        //Will impliment later
        return farmAddress.contains(location);
    }
    
    @Override
    public String toString() {
        return "Farmer: " + super.toString() + " | Farm: " + farmAddress;
    }
}
