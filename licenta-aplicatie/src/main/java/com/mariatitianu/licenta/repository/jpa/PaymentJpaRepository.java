package com.mariatitianu.licenta.repository.jpa;

import com.mariatitianu.licenta.entity.CustomerPayment;
import com.mariatitianu.licenta.repository.PaymentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface PaymentJpaRepository extends PaymentRepository, JpaRepository<CustomerPayment, Long> {
    // All methods are inherited from both interfaces
    // Spring Data JPA will auto-implement them
}