package marketplace;

import java.time.LocalDate;
//import java.time.Month;
import java.util.*;
public class TestMeketPlace {
    public static void main(String[] args) {
        //Product test
//        try {
//            Product p1= new Product("p101", "Carrots", "Organic", 4.5, 20, LocalDate.of(2025, 6, 5));
//            System.out.println(p1);
//            
//            p1.setPrice(-9.8);
//            p1.setHarvestDate(LocalDate.of(2024, 4, 12));
//        } catch (IllegalArgumentException ex) {
//            System.out.println("Error: " + ex.getMessage());
//        }
        
        //User test
//        Product carrots = new Product("P101", "Carrots", "Organic", 4.5, 30, LocalDate.of(2025, 6, 5));
//
//        Farmer f1 = new Farmer("F01", "Ali", "ali@farm.com", "Al Ain");
//        f1.addProduct(carrots);
//        System.out.println(f1);
//        System.out.println(f1.isInServiceArea("Al Ain"));
//
//        Customer c1 = new Customer("C01", "Zara", "zara@gmail.com", "Dubai");
//        c1.addToCart(carrots);
//        System.out.println(c1);
//        System.out.println(c1.isInServiceArea("Sharjah"));
        
        //Delivery System test
         DeliverySystem system = new DeliverySystem();

        Farmer f1 = new Farmer("F01", "Ali", "ali@farm.com", "Al Ain");
        Product p1 = new Product("P101", "Carrots", "Organic", 4.5, 30, LocalDate.of(2025, 6, 5));
        f1.addProduct(p1);
        
        Farmer f2 = new Farmer("F02", "Amir", "amir@farm.com", "Abu Dhabi");
        Product p2 = new Product("P102", "Banana", "Organic", 4.5, 30, LocalDate.of(2025, 6, 5));
        f2.addProduct(p2);
        
        Customer c1 = new Customer("C01", "Zara", "zara@gmail.com", "Dubai");
        Customer c2 = new Customer("C02", "Ahmed", "ahmed@gmail.com", "Fujaira");
        system.registerFarmer(f1);
        system.registerFarmer(f2);
        system.registerCustomer(c1);
        system.registerCustomer(c2);

        system.listAvailableProducts();
        
        system.processOrder("C01", "P101", 10);
        system.processOrder("C02", "P101", 25);
        system.listAvailableProducts();
        
        ProductSearchEngine searchEngine = new ProductSearchEngine(system.getFarmers());

        System.out.println("Seasonal Search:");
        searchEngine.searchBySeason(LocalDate.of(2025, 6, 1)).forEach(System.out::println);

        System.out.println("\nProximity Search:");
        List<Product> proximityResults = searchEngine.searchByProximity("Al Ain", 50);
        for (Product p : proximityResults) {
            System.out.println(p);
        }

        System.out.println("\nCategory Search:");
        List<Product> categoryResults = searchEngine.searchByCategory("organic");
        for (Product p : categoryResults) {
            System.out.println(p);
}


    }  
}
