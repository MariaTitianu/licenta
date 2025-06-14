package com.mariatitianu.licenta.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Profile("jdbc")
public class TestController {
    
    private final DataSource dataSource;
    
    // Vulnerable endpoint for testing SQL injection
    @DeleteMapping("/products/by-id/{id}")
    public Map<String, Object> deleteProductById(@PathVariable String id) {
        // VULNERABLE: Direct string concatenation without validation
        String sql = "DELETE FROM products WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            
            return Map.of(
                "success", true,
                "rowsAffected", rowsAffected,
                "executedQuery", sql
            );
        } catch (SQLException e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "attemptedQuery", sql
            );
        }
    }
    
    // Another vulnerable endpoint with string parameter
    @DeleteMapping("/products/by-category/{category}")
    public Map<String, Object> deleteProductsByCategory(@PathVariable String category) {
        // VULNERABLE: Direct string concatenation
        String sql = "DELETE FROM products WHERE category = '" + category + "'";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            
            return Map.of(
                "success", true,
                "rowsAffected", rowsAffected,
                "executedQuery", sql
            );
        } catch (SQLException e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "attemptedQuery", sql
            );
        }
    }
}