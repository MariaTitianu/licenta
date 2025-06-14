package com.mariatitianu.licenta.repository.jdbc;

import com.mariatitianu.licenta.entity.CustomerPayment;
import com.mariatitianu.licenta.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Profile("jdbc")
@RequiredArgsConstructor
public class PaymentJdbcRepository implements PaymentRepository {
    
    private final DataSource dataSource;
    
    @Override
    public List<CustomerPayment> findAll() {
        List<CustomerPayment> payments = new ArrayList<>();
        String sql = "SELECT * FROM customer_payments ORDER BY id";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching payments", e);
        }
        
        return payments;
    }
    
    @Override
    public Optional<CustomerPayment> findById(Long id) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT * FROM customer_payments WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching payment", e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public CustomerPayment save(CustomerPayment payment) {
        if (payment.getId() == null) {
            return insert(payment);
        } else {
            return update(payment);
        }
    }
    
    private CustomerPayment insert(CustomerPayment payment) {
        // VULNERABLE: Direct string concatenation with dates and amounts
        String sql = String.format(
            "INSERT INTO customer_payments (customer_name, card_last_four_digits, " +
            "card_type, amount, payment_date) VALUES ('%s', '%s', '%s', %s, '%s')",
            payment.getCustomerName(),
            payment.getCardLastFourDigits(),
            payment.getCardType(),
            payment.getAmount(),
            payment.getPaymentDate()
        );
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    payment.setId(generatedKeys.getLong(1));
                }
            }
            
            return payment;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting payment", e);
        }
    }
    
    private CustomerPayment update(CustomerPayment payment) {
        // VULNERABLE: Direct string concatenation
        String sql = String.format(
            "UPDATE customer_payments SET customer_name = '%s', " +
            "card_last_four_digits = '%s', card_type = '%s', " +
            "amount = %s, payment_date = '%s' WHERE id = %d",
            payment.getCustomerName(),
            payment.getCardLastFourDigits(),
            payment.getCardType(),
            payment.getAmount(),
            payment.getPaymentDate(),
            payment.getId()
        );
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new RuntimeException("Payment not found: " + payment.getId());
            }
            
            return payment;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating payment", e);
        }
    }
    
    @Override
    public void deleteById(Long id) {
        // VULNERABLE: Direct string concatenation
        String sql = "DELETE FROM customer_payments WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int rowsAffected = stmt.executeUpdate(sql);
            if (rowsAffected == 0) {
                throw new RuntimeException("Payment not found: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting payment", e);
        }
    }
    
    @Override
    public boolean existsById(Long id) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT COUNT(*) FROM customer_payments WHERE id = " + id;
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking payment existence", e);
        }
        
        return false;
    }
    
    @Override
    public List<CustomerPayment> findByCustomerName(String customerName) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT * FROM customer_payments WHERE customer_name = '" + 
                     customerName + "'";
        List<CustomerPayment> payments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by customer", e);
        }
        
        return payments;
    }
    
    @Override
    public List<CustomerPayment> findByCardType(String cardType) {
        // VULNERABLE: Direct string concatenation
        String sql = "SELECT * FROM customer_payments WHERE card_type = '" + 
                     cardType + "'";
        List<CustomerPayment> payments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by card type", e);
        }
        
        return payments;
    }
    
    @Override
    public List<CustomerPayment> findByAmountGreaterThan(BigDecimal amount) {
        // VULNERABLE: Direct numeric injection
        String sql = "SELECT * FROM customer_payments WHERE amount > " + amount;
        List<CustomerPayment> payments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by amount", e);
        }
        
        return payments;
    }
    
    @Override
    public List<CustomerPayment> findByPaymentDateBetween(LocalDateTime startDate, 
                                                          LocalDateTime endDate) {
        // VULNERABLE: Direct date injection
        String sql = String.format(
            "SELECT * FROM customer_payments WHERE payment_date BETWEEN '%s' AND '%s'",
            startDate, endDate
        );
        List<CustomerPayment> payments = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by date range", e);
        }
        
        return payments;
    }
    
    private CustomerPayment mapRowToPayment(ResultSet rs) throws SQLException {
        CustomerPayment payment = new CustomerPayment();
        payment.setId(rs.getLong("id"));
        payment.setCustomerName(rs.getString("customer_name"));
        payment.setCardLastFourDigits(rs.getString("card_last_four_digits"));
        payment.setCardType(rs.getString("card_type"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentDate(rs.getTimestamp("payment_date").toLocalDateTime());
        return payment;
    }
}