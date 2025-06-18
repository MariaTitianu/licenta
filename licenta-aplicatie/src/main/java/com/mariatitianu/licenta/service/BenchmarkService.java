package com.mariatitianu.licenta.service;

import com.mariatitianu.licenta.dto.BenchmarkRequest;
import com.mariatitianu.licenta.dto.BenchmarkResult;
import com.mariatitianu.licenta.dto.BenchmarkResult.OperationResult;
import com.mariatitianu.licenta.dto.MultiBenchmarkRequest;
import com.mariatitianu.licenta.dto.MultiBenchmarkResult;
import com.mariatitianu.licenta.entity.Product;
import com.mariatitianu.licenta.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenchmarkService {
    
    private final ProductRepository productRepository;
    private final ProtectionService protectionService;
    
    public BenchmarkResult runBenchmark(BenchmarkRequest request) {
        BenchmarkResult result = new BenchmarkResult();
        result.setIterations(request.getIterations());
        result.setTableName(request.getTableName());
        result.setProtectionState(request.getProtectionState());
        
        // Set protection state if requested
        if (!"skip".equals(request.getProtectionState())) {
            try {
                if ("protected".equals(request.getProtectionState())) {
                    protectionService.protectTable(request.getTableName());
                } else {
                    protectionService.unprotectTable(request.getTableName());
                }
                result.setProtectionState(request.getProtectionState());
            } catch (Exception e) {
                log.error("Failed to set protection state", e);
            }
        }
        
        long totalStartTime = System.currentTimeMillis();
        
        // Run requested operations
        switch (request.getOperation().toLowerCase()) {
            case "select":
                result.getOperations().put("select", benchmarkSelect(request.getIterations()));
                break;
            case "insert":
                result.getOperations().put("insert", benchmarkInsert(request.getIterations()));
                break;
            case "update":
                result.getOperations().put("update", benchmarkUpdate(request.getIterations()));
                break;
            case "delete":
                result.getOperations().put("delete", benchmarkDelete(request.getIterations()));
                break;
            case "all":
                result.getOperations().put("select", benchmarkSelect(request.getIterations()));
                result.getOperations().put("insert", benchmarkInsert(request.getIterations()));
                result.getOperations().put("update", benchmarkUpdate(request.getIterations()));
                result.getOperations().put("delete", benchmarkDelete(request.getIterations()));
                break;
            default:
                throw new IllegalArgumentException("Invalid operation: " + request.getOperation());
        }
        
        result.setTotalTime(System.currentTimeMillis() - totalStartTime);
        return result;
    }
    
    private OperationResult benchmarkSelect(int iterations) {
        OperationResult result = new OperationResult();
        result.setOperation("select");
        
        // Get existing product IDs
        List<Product> allProducts = productRepository.findAll();
        if (allProducts.isEmpty()) {
            result.setError("No products found for SELECT benchmark");
            return result;
        }
        
        List<Long> productIds = allProducts.stream()
            .map(Product::getId)
            .toList();
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                // Select a random product
                Long randomId = productIds.get(ThreadLocalRandom.current().nextInt(productIds.size()));
                Optional<Product> product = productRepository.findById(randomId);
                if (product.isPresent()) {
                    successCount++;
                } else {
                    errorCount++;
                }
            } catch (Exception e) {
                errorCount++;
                log.debug("SELECT operation failed: {}", e.getMessage());
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setAverageTimePerOperation(iterations > 0 ? (double) executionTime / iterations : 0);
        
        return result;
    }
    
    @Transactional
    private OperationResult benchmarkInsert(int iterations) {
        OperationResult result = new OperationResult();
        result.setOperation("insert");
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int errorCount = 0;
        List<Long> createdIds = new ArrayList<>();
        
        for (int i = 0; i < iterations; i++) {
            try {
                Product product = new Product();
                product.setName("Benchmark Product " + System.currentTimeMillis() + "_" + i);
                product.setCategory("Benchmark");
                product.setPrice(new BigDecimal("99.99"));
                product.setStockQuantity(100);
                product.setDescription("Benchmark test product");
                
                Product saved = productRepository.save(product);
                createdIds.add(saved.getId());
                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.debug("INSERT operation failed: {}", e.getMessage());
            }
        }
        
        // Clean up created products
        for (Long id : createdIds) {
            try {
                productRepository.deleteById(id);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setAverageTimePerOperation(iterations > 0 ? (double) executionTime / iterations : 0);
        
        return result;
    }
    
    @Transactional
    private OperationResult benchmarkUpdate(int iterations) {
        OperationResult result = new OperationResult();
        result.setOperation("update");
        
        // Get existing products
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            result.setError("No products found for UPDATE benchmark");
            return result;
        }
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int blockedCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < iterations; i++) {
            try {
                // Update a random product
                Product randomProduct = products.get(ThreadLocalRandom.current().nextInt(products.size()));
                Optional<Product> productOpt = productRepository.findById(randomProduct.getId());
                
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setDescription("Updated at " + System.currentTimeMillis() + "_" + i);
                    productRepository.save(product);
                    successCount++;
                }
            } catch (Exception e) {
                if (e.getMessage() != null && 
                    (e.getMessage().contains("not allowed") || 
                     e.getMessage().contains("protected") ||
                     e.getMessage().contains("blocked"))) {
                    blockedCount++;
                } else {
                    errorCount++;
                }
                log.debug("UPDATE operation failed: {}", e.getMessage());
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);
        result.setSuccessCount(successCount);
        result.setBlockedCount(blockedCount);
        result.setErrorCount(errorCount);
        result.setAverageTimePerOperation(iterations > 0 ? (double) executionTime / iterations : 0);
        
        return result;
    }
    
    @Transactional
    private OperationResult benchmarkDelete(int iterations) {
        OperationResult result = new OperationResult();
        result.setOperation("delete");
        
        // First, create products to delete
        List<Long> createdIds = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            try {
                Product product = new Product();
                product.setName("Delete Benchmark " + System.currentTimeMillis() + "_" + i);
                product.setCategory("DeleteBenchmark");
                product.setPrice(new BigDecimal("1.99"));
                product.setStockQuantity(1);
                product.setDescription("To be deleted");
                
                Product saved = productRepository.save(product);
                createdIds.add(saved.getId());
            } catch (Exception e) {
                log.debug("Failed to create product for delete benchmark: {}", e.getMessage());
            }
        }
        
        if (createdIds.isEmpty()) {
            result.setError("Failed to create products for DELETE benchmark");
            return result;
        }
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int blockedCount = 0;
        int errorCount = 0;
        
        for (Long id : createdIds) {
            try {
                productRepository.deleteById(id);
                successCount++;
            } catch (Exception e) {
                if (e.getMessage() != null && 
                    (e.getMessage().contains("not allowed") || 
                     e.getMessage().contains("protected") ||
                     e.getMessage().contains("blocked"))) {
                    blockedCount++;
                } else {
                    errorCount++;
                }
                log.debug("DELETE operation failed: {}", e.getMessage());
            }
        }
        
        // Clean up any remaining products
        for (Long id : createdIds) {
            try {
                if (productRepository.existsById(id)) {
                    // If protection was disabled, try to clean up
                    productRepository.deleteById(id);
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        result.setExecutionTime(executionTime);
        result.setSuccessCount(successCount);
        result.setBlockedCount(blockedCount);
        result.setErrorCount(errorCount);
        result.setAverageTimePerOperation(iterations > 0 ? (double) executionTime / iterations : 0);
        
        return result;
    }
    
    public MultiBenchmarkResult runMultiBenchmark(MultiBenchmarkRequest request) {
        MultiBenchmarkResult result = new MultiBenchmarkResult();
        result.setTotalIterations(request.getIterations());
        
        Map<String, MultiBenchmarkResult.OperationResult> operationResults = new HashMap<>();
        long totalStartTime = System.currentTimeMillis();
        
        // Ensure tables are unprotected for benchmarking on pg_warden backends
        try {
            protectionService.unprotectTable("products");
            protectionService.unprotectTable("customer_payments");
        } catch (Exception e) {
            log.warn("Could not unprotect tables - might be vanilla PostgreSQL: {}", e.getMessage());
        }
        
        for (String operation : request.getOperations()) {
            MultiBenchmarkResult.OperationResult opResult = new MultiBenchmarkResult.OperationResult();
            opResult.setIterations(request.getIterations());
            
            try {
                // Run the benchmark for this operation
                OperationResult benchmarkResult = null;
                switch (operation.toUpperCase()) {
                    case "SELECT":
                        benchmarkResult = benchmarkSelect(request.getIterations());
                        break;
                    case "INSERT":
                        benchmarkResult = benchmarkInsert(request.getIterations());
                        break;
                    case "UPDATE":
                        benchmarkResult = benchmarkUpdate(request.getIterations());
                        break;
                    case "DELETE":
                        benchmarkResult = benchmarkDelete(request.getIterations());
                        break;
                    default:
                        opResult.setError("Unknown operation: " + operation);
                        operationResults.put(operation, opResult);
                        continue;
                }
                
                if (benchmarkResult != null) {
                    opResult.setTotalTimeMs(benchmarkResult.getExecutionTime());
                    opResult.setAvgTimeMs(benchmarkResult.getAverageTimePerOperation());
                    if (benchmarkResult.getExecutionTime() > 0) {
                        opResult.setOpsPerSecond((double) request.getIterations() * 1000 / benchmarkResult.getExecutionTime());
                    }
                    if (benchmarkResult.getError() != null) {
                        opResult.setError(benchmarkResult.getError());
                    }
                }
            } catch (Exception e) {
                opResult.setError("Failed to run " + operation + ": " + e.getMessage());
                log.error("Benchmark failed for operation: " + operation, e);
            }
            
            operationResults.put(operation, opResult);
        }
        
        result.setTotalTimeMs(System.currentTimeMillis() - totalStartTime);
        result.setOperationResults(operationResults);
        
        return result;
    }
}