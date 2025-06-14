package com.mariatitianu.licenta.service;

import com.mariatitianu.licenta.entity.UnprotectedTable;
import com.mariatitianu.licenta.repository.ProtectionRepository;
import com.mariatitianu.licenta.repository.UnprotectedTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProtectionService {
    
    private final ProtectionRepository protectionRepository;
    private final UnprotectedTableRepository unprotectedTableRepository;
    
    public boolean protectTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        return protectionRepository.protectTable(tableName.trim());
    }
    
    public boolean unprotectTable(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        return protectionRepository.unprotectTable(tableName.trim());
    }
    
    public List<String> getUnprotectedTables() {
        // Simple JPA query - get all entities
        List<UnprotectedTable> unprotectedTables = unprotectedTableRepository.findAllUnprotectedTables();
        
        // Extract table names and sort
        return unprotectedTables.stream()
            .map(UnprotectedTable::getTableName)
            .sorted()
            .collect(Collectors.toList());
    }
    
    public boolean isTableProtected(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
        
        // First check if table exists
        if (!tableExists(tableName.trim())) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist");
        }
        
        // Get list of unprotected tables and check if this table is in it
        List<String> unprotectedTables = getUnprotectedTables();
        return !unprotectedTables.contains(tableName.trim());
    }
    
    private boolean tableExists(String tableName) {
        try {
            Integer count = unprotectedTableRepository.checkTableExists(tableName);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getTableProtectionStatus(String tableName) {
        return isTableProtected(tableName) ? "PROTECTED" : "UNPROTECTED";
    }
    
    public Map<String, Object> getProtectionSummary() {
        List<String> unprotectedTables = getUnprotectedTables();
        
        // Get total number of user tables
        int totalTables = 0;
        try {
            Integer count = unprotectedTableRepository.countUserTables();
            totalTables = count != null ? count : 0;
        } catch (Exception e) {
            // Silently handle error
        }
        
        // Build the summary directly
        Map<String, Object> summary = new HashMap<>();
        summary.put("unprotectedTables", unprotectedTables);
        summary.put("unprotectedCount", unprotectedTables.size());
        
        // Determine overall status
        String overallStatus;
        if (unprotectedTables.isEmpty()) {
            overallStatus = "ALL_TABLES_PROTECTED";
        } else if (totalTables > 0 && unprotectedTables.size() >= totalTables) {
            overallStatus = "ALL_TABLES_UNPROTECTED";
        } else {
            overallStatus = "SOME_TABLES_UNPROTECTED";
        }
        summary.put("overallStatus", overallStatus);
        
        return summary;
    }
    
    public Map<String, String> protectTableWithResponse(String tableName) {
        boolean changed = protectTable(tableName);
        if (changed) {
            return Map.of(
                "status", "success",
                "message", "Table '" + tableName + "' is now protected",
                "tableName", tableName,
                "protectionStatus", "PROTECTED",
                "changed", "true"
            );
        } else {
            return Map.of(
                "status", "success",
                "message", "Table '" + tableName + "' is already protected",
                "tableName", tableName,
                "protectionStatus", "PROTECTED",
                "changed", "false"
            );
        }
    }
    
    public Map<String, String> unprotectTableWithResponse(String tableName) {
        boolean changed = unprotectTable(tableName);
        if (changed) {
            return Map.of(
                "status", "success",
                "message", "Table '" + tableName + "' is now unprotected",
                "tableName", tableName,
                "protectionStatus", "UNPROTECTED",
                "changed", "true"
            );
        } else {
            return Map.of(
                "status", "success",
                "message", "Table '" + tableName + "' is already unprotected",
                "tableName", tableName,
                "protectionStatus", "UNPROTECTED",
                "changed", "false"
            );
        }
    }
    
    public Map<String, Object> getTableProtectionStatusResponse(String tableName) {
        boolean isProtected = isTableProtected(tableName);
        String status = getTableProtectionStatus(tableName);
        
        return Map.of(
            "tableName", tableName,
            "isProtected", isProtected,
            "protectionStatus", status
        );
    }
    
    public Map<String, Object> getProtectionSummaryWithNote() {
        Map<String, Object> summary = getProtectionSummary();
        
        // Add note if all tables are protected
        if ((int) summary.get("unprotectedCount") == 0) {
            summary.put("note", "All tables are protected. Use POST /api/protection/unprotect/{tableName} to unprotect a table.");
        }
        
        return summary;
    }
}