package com.mariatitianu.licenta.repository.jpa;

import com.mariatitianu.licenta.entity.Product;
import com.mariatitianu.licenta.repository.ProductRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface ProductJpaRepository extends ProductRepository, JpaRepository<Product, Long> {
    // All methods are inherited from both interfaces
    // Spring Data JPA will auto-implement them
    
    @Query("SELECT MIN(p.id) FROM Product p")
    Long findMinId();
    
    @Query("SELECT MAX(p.id) FROM Product p")
    Long findMaxId();
}