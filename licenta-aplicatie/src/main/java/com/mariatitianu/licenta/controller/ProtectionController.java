package com.mariatitianu.licenta.controller;

import com.mariatitianu.licenta.service.ProtectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/protection")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProtectionController {
    
    private final ProtectionService protectionService;
    
    @PostMapping("/protect/{tableName}")
    public ResponseEntity<Map<String, String>> protectTable(@PathVariable String tableName) {
        try {
            return ResponseEntity.ok(protectionService.protectTableWithResponse(tableName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to protect table: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/unprotect/{tableName}")
    public ResponseEntity<Map<String, String>> unprotectTable(@PathVariable String tableName) {
        try {
            return ResponseEntity.ok(protectionService.unprotectTableWithResponse(tableName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to unprotect table: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/status/{tableName}")
    public ResponseEntity<Map<String, Object>> getTableProtectionStatus(@PathVariable String tableName) {
        try {
            return ResponseEntity.ok(protectionService.getTableProtectionStatusResponse(tableName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to get protection status: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/unprotected")
    public ResponseEntity<List<String>> getUnprotectedTables() {
        try {
            return ResponseEntity.ok(protectionService.getUnprotectedTables());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getProtectionSummary() {
        try {
            return ResponseEntity.ok(protectionService.getProtectionSummaryWithNote());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to get protection summary: " + e.getMessage()
            ));
        }
    }
    
}