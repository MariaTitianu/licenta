package com.mariatitianu.licenta.controller;

import com.mariatitianu.licenta.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LogController {
    
    private final LogService logService;
    
    @GetMapping("/operations")
    public ResponseEntity<List<LogService.OperationLog>> getRecentOperations(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<LogService.OperationLog> logs = logService.getRecentOperations(limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/operations/recent")
    public ResponseEntity<List<LogService.OperationLog>> getRecentOperations() {
        try {
            List<LogService.OperationLog> logs = logService.getRecentOperations();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/operations/summary")
    public ResponseEntity<Map<String, Object>> getOperationsSummary(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            Map<String, Object> summary = logService.getOperationsSummary(limit);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/operations/blocked")
    public ResponseEntity<List<LogService.OperationLog>> getBlockedOperations(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<LogService.OperationLog> blockedLogs = logService.getBlockedOperations(limit);
            return ResponseEntity.ok(blockedLogs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/operations/allowed")
    public ResponseEntity<List<LogService.OperationLog>> getAllowedOperations(
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<LogService.OperationLog> allowedLogs = logService.getAllowedOperations(limit);
            return ResponseEntity.ok(allowedLogs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/operations/table/{tableName}")
    public ResponseEntity<List<LogService.OperationLog>> getOperationsByTable(
            @PathVariable String tableName,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<LogService.OperationLog> tableLogs = logService.getOperationsByTable(tableName, limit);
            return ResponseEntity.ok(tableLogs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}