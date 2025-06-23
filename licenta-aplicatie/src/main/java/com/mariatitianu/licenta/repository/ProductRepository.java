package com.mariatitianu.licenta.repository;

import com.mariatitianu.licenta.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    
    List<Product> findAll();
    
    Optional<Product> findById(Long id);
    
    Product save(Product product);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    List<Product> findByCategory(String category);
    
    Optional<Product> findByName(String name);
    
    List<Product> findByNameContainingIgnoreCase(String name);
    
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    
    Long findMinId();
    
    Long findMaxId();
}