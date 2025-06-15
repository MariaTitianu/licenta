package com.mariatitianu.licenta.dto;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class BenchmarkResult {
    private int iterations;
    private String tableName;
    private String protectionState;
    private Map<String, OperationResult> operations = new HashMap<>();
    private long totalTime;
    
    @Data
    public static class OperationResult {
        private String operation;
        private long executionTime; // in milliseconds
        private int successCount;
        private int blockedCount;
        private int errorCount;
        private double averageTimePerOperation;
        private String error;
    }
}