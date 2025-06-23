package com.mariatitianu.licenta.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MultiBenchmarkResult {
    private int totalIterations;
    private double totalTimeMs;
    private Map<String, OperationResult> operationResults;
    
    @Data
    public static class OperationResult {
        private int iterations;
        private double totalTimeMs;
        private double avgTimeMs;
        private double opsPerSecond;
        private String error;
        private int successCount;
        private int blockedCount;
        private int errorCount;
        private double p50TimeMs;  // median
        private double p95TimeMs;
        private double p99TimeMs;
    }
}