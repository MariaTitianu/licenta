package com.mariatitianu.licenta.controller;

import com.mariatitianu.licenta.entity.CustomerPayment;
import com.mariatitianu.licenta.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @GetMapping
    public ResponseEntity<List<CustomerPayment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CustomerPayment> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<CustomerPayment> createPayment(@RequestBody CustomerPayment payment) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(payment));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CustomerPayment> updatePayment(@PathVariable Long id, @RequestBody CustomerPayment payment) {
        try {
            CustomerPayment updatedPayment = paymentService.updatePayment(id, payment);
            return ResponseEntity.ok(updatedPayment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}