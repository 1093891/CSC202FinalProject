
package marketplace;

import java.util.*;
import java.time.LocalDate;

public class ProductSearchEngine implements IProductSearch {
    private List<Farmer> farmers;
    
    public ProductSearchEngine(List<Farmer> farmers) {
        this.farmers=farmers;
    }
    
    @Override 
    public List<Product> searchBySeason(LocalDate targetDate) {
        List<Product> result= new ArrayList<>();
        for (Farmer f: farmers){
            for (Product p: f.getInventory()) {
                if (p.getHarvestDate().getMonth() == targetDate.getMonth()) {
                    result.add(p);
                }
            }
        }
        return result;
    }
    
    @Override 
    public List<Product> searchByProximity(String location, int radiusKm) {
        List<Product> result= new ArrayList<>();
        for (Farmer f: farmers) {
            if (f.isInServiceArea(location)) {
                for (Product p: f.getInventory()) {
                    result.add(p);
                }
            }
        }
        return result;
    }
    
    @Override
    public List<Product> searchByCategory(String category) {
        List<Product> result= new ArrayList<>();
        for (Farmer f: farmers) {
            for (Product p: f.getInventory()) {
                if (p.getDescription().toLowerCase().contains(category.toLowerCase())) {
                    result.add(p);
                }
            }
        }
        return result;
    }
}
