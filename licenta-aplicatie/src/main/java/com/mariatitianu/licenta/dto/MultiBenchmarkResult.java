package com.mariatitianu.licenta.dto;

import lombok.Data;
import java.util.Map;

@Data
public class MultiBenchmarkResult {
    private int totalIterations;
    private long totalTimeMs;
    private Map<String, OperationResult> operationResults;
    
    @Data
    public static class OperationResult {
        private int iterations;
        private long totalTimeMs;
        private double avgTimeMs;
        private double opsPerSecond;
        private String error;
    }
}