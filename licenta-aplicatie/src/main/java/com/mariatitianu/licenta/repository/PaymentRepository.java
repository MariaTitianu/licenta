package com.mariatitianu.licenta.repository;

import com.mariatitianu.licenta.entity.CustomerPayment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    
    List<CustomerPayment> findAll();
    
    Optional<CustomerPayment> findById(Long id);
    
    CustomerPayment save(CustomerPayment payment);
    
    void deleteById(Long id);
    
    boolean existsById(Long id);
    
    List<CustomerPayment> findByCustomerName(String customerName);
    
    List<CustomerPayment> findByCardType(String cardType);
    
    List<CustomerPayment> findByAmountGreaterThan(BigDecimal amount);
    
    List<CustomerPayment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}