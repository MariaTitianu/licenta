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
    
    // Vulnerable endpoint for testing SQL injection on payments
    @DeleteMapping("/payments/by-id/{id}")
    public Map<String, Object> deletePaymentById(@PathVariable String id) {
        // VULNERABLE: Direct string concatenation without validation
        String sql = "DELETE FROM customer_payments WHERE id = " + id;
        
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
    @DeleteMapping("/payments/by-customer/{customer}")
    public Map<String, Object> deletePaymentsByCustomer(@PathVariable String customer) {
        // VULNERABLE: Direct string concatenation
        String sql = "DELETE FROM customer_payments WHERE customer_name = '" + customer + "'";
        
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