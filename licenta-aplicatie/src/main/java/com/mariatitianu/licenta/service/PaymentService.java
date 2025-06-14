package com.mariatitianu.licenta.service;

import com.mariatitianu.licenta.entity.CustomerPayment;
import com.mariatitianu.licenta.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    
    @Transactional(readOnly = true)
    public List<CustomerPayment> getAllPayments() {
        return paymentRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<CustomerPayment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
    
    
    public CustomerPayment createPayment(CustomerPayment payment) {
        // Ensure ID is null for new payments
        if (payment.getId() != null) {
            payment.setId(null);
        }
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        return paymentRepository.save(payment);
    }
    
    public CustomerPayment updatePayment(Long id, CustomerPayment paymentDetails) {
        CustomerPayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with id: " + id));
        
        payment.setCustomerName(paymentDetails.getCustomerName());
        payment.setCardLastFourDigits(paymentDetails.getCardLastFourDigits());
        payment.setCardType(paymentDetails.getCardType());
        payment.setAmount(paymentDetails.getAmount());
        payment.setPaymentDate(paymentDetails.getPaymentDate());
        
        return paymentRepository.save(payment);
    }
    
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new RuntimeException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }
    
}