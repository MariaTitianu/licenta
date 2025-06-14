package com.mariatitianu.licenta.service;

import com.mariatitianu.licenta.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogService {
    
    private final LogRepository logRepository;
    
    public List<OperationLog> getRecentOperations(int limit) {
        if (limit <= 0) {
            limit = 50;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        
        List<Map<String, Object>> rawLogs = logRepository.getRecentOperations(limit);
        return rawLogs.stream()
                .map(this::mapToOperationLog)
                .collect(Collectors.toList());
    }
    
    public List<OperationLog> getRecentOperations() {
        return getRecentOperations(50);
    }
    
    public List<OperationLog> getAllOperations() {
        List<Map<String, Object>> rawLogs = logRepository.getAllOperations();
        return rawLogs.stream()
                .map(this::mapToOperationLog)
                .collect(Collectors.toList());
    }
    
    public List<OperationLog> getBlockedOperations(int limit) {
        if (limit <= 0) {
            limit = 50;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        
        // Fetch more logs to ensure we get enough blocked ones
        List<Map<String, Object>> rawLogs = logRepository.getRecentOperations(limit * 2);
        return rawLogs.stream()
                .map(this::mapToOperationLog)
                .filter(log -> "BLOCKED".equals(log.getStatus()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<OperationLog> getAllowedOperations(int limit) {
        if (limit <= 0) {
            limit = 50;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        
        // Fetch more logs to ensure we get enough allowed ones
        List<Map<String, Object>> rawLogs = logRepository.getRecentOperations(limit * 2);
        return rawLogs.stream()
                .map(this::mapToOperationLog)
                .filter(log -> "ALLOWED".equals(log.getStatus()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public List<OperationLog> getOperationsByTable(String tableName, int limit) {
        if (limit <= 0) {
            limit = 50;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        
        // Fetch more logs to ensure we get enough for the specific table
        List<Map<String, Object>> rawLogs = logRepository.getRecentOperations(limit * 3);
        return rawLogs.stream()
                .map(this::mapToOperationLog)
                .filter(log -> tableName.equals(log.getTableName()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> getOperationsSummary(int limit) {
        if (limit <= 0) {
            limit = 100;
        }
        if (limit > 1000) {
            limit = 1000;
        }
        
        List<OperationLog> logs = getRecentOperations(limit);
        
        long blockedCount = logs.stream().filter(log -> "BLOCKED".equals(log.getStatus())).count();
        long allowedCount = logs.size() - blockedCount;
        
        Map<String, Long> operationTypes = logs.stream()
                .collect(Collectors.groupingBy(
                        OperationLog::getOperationType,
                        Collectors.counting()
                ));
        
        Map<String, Long> statusCounts = logs.stream()
                .collect(Collectors.groupingBy(
                        OperationLog::getStatus,
                        Collectors.counting()
                ));
        
        return Map.of(
            "totalOperations", logs.size(),
            "blockedOperations", blockedCount,
            "allowedOperations", allowedCount,
            "operationTypes", operationTypes,
            "statusCounts", statusCounts
        );
    }
    
    private OperationLog mapToOperationLog(Map<String, Object> rawLog) {
        return new OperationLog(
                (String) rawLog.get("operation_type"),
                (String) rawLog.get("table_name"),
                (String) rawLog.get("log_timestamp"),
                (String) rawLog.get("user_name"),
                (String) rawLog.get("blocked_reason")
        );
    }
    
    @Getter
    @AllArgsConstructor
    public static class OperationLog {
        private final String operationType;
        private final String tableName;
        private final String operationTime;
        private final String userName;
        private final String blockedReason;
        
        public String getStatus() {
            return (blockedReason != null && !blockedReason.trim().isEmpty()) ? "BLOCKED" : "ALLOWED";
        }
    }
}