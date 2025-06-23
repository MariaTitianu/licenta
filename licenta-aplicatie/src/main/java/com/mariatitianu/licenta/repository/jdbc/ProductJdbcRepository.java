package com.mariatitianu.licenta.repository.jdbc;

import com.mariatitianu.licenta.entity.Product;
import com.mariatitianu.licenta.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jdbc")
@RequiredArgsConstructor
public class ProductJdbcRepository implements ProductRepository {
    
    private final DataSource dataSource;
    
    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products", e);
        }
        
        return products;
    }
    
    @Override
    public Optional<Product> findById(Long id) {
        // VULNERABLE: Direct string concatenation allows SQL injection
        String sql = "SELECT * FROM products WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product", e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            return insert(product);
        } else {
            return update(product);
        }
    }
    
    private Product insert(Product product) {
        // VULNERABLE: Direct string concatenation with user input
        String sql = String.format(
            "INSERT INTO products (name, category, price, stock_quantity, description) " +
            "VALUES ('%s', '%s', %s, %d, '%s')",
            product.getName(), 
            product.getCategory(),
            product.getPrice(), 
            product.getStockQuantity(),
            product.getDescription()
        );
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getLong(1));
                }
            }
            
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting product", e);
        }
    }
    
    private Product update(Product product) {
        // VULNERABLE: Direct string concatenation
        String sql = String.format(
            "UPDATE products SET name = '%s', category = '%s', price = %s, " +
            "stock_quantity = %d, description = '%s' WHERE id = %d",
            product.getName(),
            product.getCategory(),
            product.getPrice(),
            product.getStockQuantity(),
            product.getDescription(),
            product.getId()
        );
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new RuntimeException("Product not found: " + product.getId());
            }
            
            return product;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        // VULNERABLE: Direct string concatenation
        String sql = "DELETE FROM products WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new RuntimeException("Product not found: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        }
    }
    
    @Override
    public boolean existsById(Long id) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT COUNT(*) FROM products WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking product existence", e);
        }
        
        return false;
    }
    
    @Override
    public List<Product> findByCategory(String category) {
        // VULNERABLE: Direct string concatenation in WHERE clause
        String sql = "SELECT * FROM products WHERE category = '" + category + "'";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products by category", e);
        }
        
        return products;
    }
    
    @Override
    public Optional<Product> findByName(String name) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT * FROM products WHERE name = '" + name + "'";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding product by name", e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        // EXTREMELY VULNERABLE: Direct injection in LIKE clause
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE '%" + 
                     name.toLowerCase() + "%'";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products", e);
        }
        
        return products;
    }
    
    @Override
    public List<Product> findByStockQuantityGreaterThan(Integer quantity) {
        // VULNERABLE: Direct numeric injection
        String sql = "SELECT * FROM products WHERE stock_quantity > " + quantity;
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by stock", e);
        }
        
        return products;
    }
    
    @Override
    public Long findMinId() {
        String sql = "SELECT MIN(id) FROM products";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding min product ID", e);
        }
        
        return null;
    }
    
    @Override
    public Long findMaxId() {
        String sql = "SELECT MAX(id) FROM products";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding max product ID", e);
        }
        
        return null;
    }
    
    private Product mapRowToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setCategory(rs.getString("category"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        return product;
    }
}