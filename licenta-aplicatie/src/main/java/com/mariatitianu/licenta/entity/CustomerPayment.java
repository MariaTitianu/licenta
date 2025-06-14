package com.mariatitianu.licenta.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(name = "card_last_four_digits")
    private String cardLastFourDigits;
    
    @Column(name = "card_type")
    private String cardType;
    
    private BigDecimal amount;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}