package com.onlinemarketplace.service;

import com.onlinemarketplace.model.Product;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface for searching and filtering products in the marketplace.
 * Defines methods for searching by seasonality, proximity, and category.
 */
public interface IProductSearch {

    /**
     * Searches for products that are currently in season relative to a given date.
     *
     * @param date The reference date to determine seasonality.
     * @return A list of products considered in season.
     */
    List<Product> searchBySeason(LocalDate date);

    /**
     * Filters products based on their farm's proximity to a given customer address.
     * This is a simulated proximity search.
     *
     * @param customerAddress The customer's delivery address.
     * @param radiusKm The maximum radius (in kilometers) from the customer's address to search for farms.
     * @return A list of products from farms within the specified radius, sorted by proximity.
     */
    List<Product> searchByProximity(String customerAddress, int radiusKm);

    /**
     * Finds products belonging to a specific category.
     *
     * @param category The category to search for (e.g., dairy, vegetables).
     * @return A list of products matching the given category.
     */
    List<Product> searchByCategory(String category);
}