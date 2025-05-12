
package marketplace;

import java.time.LocalDate;
import java.util.List;
public interface IProductSearch {
    List<Product> searchBySeason(LocalDate targetDate);
    List<Product> searchByProximity(String location, int radiusKm);
    List<Product> searchByCategory(String cataory);
}
