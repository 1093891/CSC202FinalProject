package com.onlinemarketplace.service;

import com.onlinemarketplace.model.Farmer;
import com.onlinemarketplace.model.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
// Removed: Comparator, Collectors

/**
 * Implements the IProductSearch interface, providing concrete search and filtering logic.
 * This class uses basic loops and conditional statements for search and sorting.
 */
public class ProductSearchEngine implements IProductSearch {

    private final DeliverySystem deliverySystem; // Reference to the core system to access products and farmers

    /**
     * Constructs a ProductSearchEngine with a reference to the DeliverySystem.
     *
     * @param deliverySystem The DeliverySystem instance to query for products and farmers.
     */
    public ProductSearchEngine(DeliverySystem deliverySystem) {
        this.deliverySystem = deliverySystem;
    }

    /**
     * Searches for products that are currently in season.
     * Prioritizes products that are most recently harvested using a basic bubble sort.
     *
     * @param date The current date to check seasonality against.
     * @return A list of in-season products, sorted by harvest date (most recent first).
     */
    @Override
    public List<Product> searchBySeason(LocalDate date) {
        List<Product> inSeasonProducts = new ArrayList<>();
        List<Product> allProducts = deliverySystem.getAllProducts(); // Get a copy

        for (Product product : allProducts) {
            if (product.isInSeason(date)) {
                inSeasonProducts.add(product);
            }
        }

        // Basic Bubble Sort by Harvest Date (descending - most recent first)
        for (int i = 0; i < inSeasonProducts.size() - 1; i++) {
            for (int j = 0; j < inSeasonProducts.size() - i - 1; j++) {
                if (inSeasonProducts.get(j).getHarvestDate().isBefore(inSeasonProducts.get(j + 1).getHarvestDate())) {
                    // Swap
                    Product temp = inSeasonProducts.get(j);
                    inSeasonProducts.set(j, inSeasonProducts.get(j + 1));
                    inSeasonProducts.set(j + 1, temp);
                }
            }
        }
        return inSeasonProducts;
    }

    /**
     * Filters products based on their farm's proximity to a given customer address.
     * This is a simulated proximity search. It sorts by "distance" (simulated by address similarity).
     *
     * @param customerAddress The customer's delivery address.
     * @param radiusKm The maximum radius (in kilometers) from the customer's address to search for farms.
     * @return A list of products from farms within the specified radius, sorted by proximity.
     */
    @Override
    public List<Product> searchByProximity(String customerAddress, int radiusKm) {
        List<Product> proximateProducts = new ArrayList<>();
        List<Farmer> allFarmers = deliverySystem.getAllFarmers(); // Get a copy

        // Find nearby farmers and store them with their simulated distance
        List<FarmerWithDistance> farmersWithDistances = new ArrayList<>();
        for (Farmer farmer : allFarmers) {
            if (simulateProximity(customerAddress, farmer.getFarmAddress(), radiusKm)) {
                int simulatedDistance = calculateSimulatedDistance(customerAddress, farmer.getFarmAddress());
                farmersWithDistances.add(new FarmerWithDistance(farmer, simulatedDistance));
            }
        }

        // Basic Bubble Sort farmers by simulated distance (ascending)
        for (int i = 0; i < farmersWithDistances.size() - 1; i++) {
            for (int j = 0; j < farmersWithDistances.size() - i - 1; j++) {
                if (farmersWithDistances.get(j).getSimulatedDistance() > farmersWithDistances.get(j + 1).getSimulatedDistance()) {
                    // Swap
                    FarmerWithDistance temp = farmersWithDistances.get(j);
                    farmersWithDistances.set(j, farmersWithDistances.get(j + 1));
                    farmersWithDistances.set(j + 1, temp);
                }
            }
        }

        // Collect products from these sorted nearby farmers
        for (FarmerWithDistance fwd : farmersWithDistances) {
            for (Product product : fwd.getFarmer().getAvailableProducts()) { // getAvailableProducts returns a List
                proximateProducts.add(product);
            }
        }
        return proximateProducts;
    }

    /**
     * Helper class for sorting farmers by simulated distance.
     */
    private static class FarmerWithDistance {
        private Farmer farmer;
        private int simulatedDistance;

        public FarmerWithDistance(Farmer farmer, int simulatedDistance) {
            this.farmer = farmer;
            this.simulatedDistance = simulatedDistance;
        }

        public Farmer getFarmer() {
            return farmer;
        }

        public int getSimulatedDistance() {
            return simulatedDistance;
        }
    }


    /**
     * Simulates proximity based on address string similarity and a radius.
     * A more robust solution would use geocoding APIs.
     *
     * @param addr1 Address 1.
     * @param addr2 Address 2.
     * @param radiusKm Simulated radius.
     * @return true if addresses are "close" based on simulation.
     */
    private boolean simulateProximity(String addr1, String addr2, int radiusKm) {
        // Very basic simulation: if addresses share a common word (e.g., city), consider them "close".
        // The radiusKm can influence how "strict" this check is.
        if (addr1 == null || addr2 == null) return false;
        String[] words1 = addr1.toLowerCase().split("\\W+");
        String[] words2 = addr2.toLowerCase().split("\\W+");

        for (String w1 : words1) {
            for (String w2 : words2) {
                // If they share a significant common word (e.g., city name), consider them close.
                // For a radius, we can make this more lenient or strict.
                if (w1.length() > 2 && w1.equals(w2)) {
                    return true; // Found a common significant word
                }
            }
        }
        return false;
    }

    /**
     * Calculates a simulated distance between two addresses.
     * Lower value means closer.
     *
     * @param addr1 Address 1.
     * @param addr2 Address 2.
     * @return An integer representing simulated distance.
     */
    private int calculateSimulatedDistance(String addr1, String addr2) {
        // This is a very simplistic heuristic. A real distance would be geographic.
        // Here, we can use string difference or just return a constant for simplicity if they are "proximate".
        // For sorting, a simple length difference or Levenshtein distance could be used.
        if (addr1 == null || addr2 == null) return Integer.MAX_VALUE;
        return Math.abs(addr1.length() - addr2.length()); // Shorter difference implies closer
    }


    /**
     * Finds products belonging to a specific category.
     * Highlights organic options by prioritizing them in the returned list using a basic bubble sort.
     *
     * @param category The category to search for (e.g., "vegetables", "fruits", "dairy").
     * @return A list of products matching the given category, with organic options appearing first.
     */
    @Override
    public List<Product> searchByCategory(String category) {
        List<Product> matchingProducts = new ArrayList<>();
        List<Product> allProducts = deliverySystem.getAllProducts(); // Get a copy

        String lowerCaseCategory = (category != null) ? category.toLowerCase() : "";

        for (Product product : allProducts) {
            if (product.getDescription().toLowerCase().contains(lowerCaseCategory) ||
                    product.getName().toLowerCase().contains(lowerCaseCategory)) {
                matchingProducts.add(product);
            }
        }

        // Basic Bubble Sort: Organic first
        for (int i = 0; i < matchingProducts.size() - 1; i++) {
            for (int j = 0; j < matchingProducts.size() - i - 1; j++) {
                boolean isOrganicJ = matchingProducts.get(j).getDescription().toLowerCase().contains("organic");
                boolean isOrganicJPlus1 = matchingProducts.get(j + 1).getDescription().toLowerCase().contains("organic");

                if (!isOrganicJ && isOrganicJPlus1) { // If J is not organic but J+1 is, swap them
                    Product temp = matchingProducts.get(j);
                    matchingProducts.set(j, matchingProducts.get(j + 1));
                    matchingProducts.set(j + 1, temp);
                }
            }
        }
        return matchingProducts;
    }

    /**
     * Finds products by a general search term, combining various criteria.
     *
     * @param searchTerm The term to search for.
     * @return A list of products matching the search term in name, description, or ID.
     */
    public List<Product> searchProducts(String searchTerm) {
        List<Product> foundProducts = new ArrayList<>();
        List<Product> allProducts = deliverySystem.getAllProducts(); // Get a copy

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return allProducts; // Return all if search term is empty
        }
        String lowerCaseSearchTerm = searchTerm.toLowerCase();

        for (Product p : allProducts) {
            if (p.getName().toLowerCase().contains(lowerCaseSearchTerm) ||
                    p.getDescription().toLowerCase().contains(lowerCaseSearchTerm) ||
                    p.getProductId().toLowerCase().contains(lowerCaseSearchTerm)) {
                foundProducts.add(p);
            }
        }
        return foundProducts;
    }
}