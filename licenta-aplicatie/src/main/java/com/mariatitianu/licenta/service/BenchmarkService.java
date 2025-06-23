package com.mariatitianu.licenta.service;

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
    
    // Warm-up iterations to stabilize JVM performance
    private static final int WARMUP_ITERATIONS = 100;
    
    public MultiBenchmarkResult runMultiBenchmark(MultiBenchmarkRequest request) {
        MultiBenchmarkResult result = new MultiBenchmarkResult();
        result.setTotalIterations(request.getIterations());
        
        Map<String, MultiBenchmarkResult.OperationResult> operationResults = new HashMap<>();
        long totalStartTime = System.nanoTime();
        
        // Only unprotect products table if UPDATE or DELETE operations are included
        boolean needsUnprotection = request.getOperations().stream()
            .anyMatch(op -> op.equalsIgnoreCase("UPDATE") || op.equalsIgnoreCase("DELETE"));
        
        boolean wasProtected = false;
        if (needsUnprotection) {
            try {
                // Check if table was protected before unprotecting
                wasProtected = protectionService.isTableProtected("products");
                if (wasProtected) {
                    protectionService.unprotectTable("products");
                    log.info("Temporarily unprotected products table for UPDATE/DELETE benchmarks");
                }
            } catch (Exception e) {
                log.warn("Could not check/unprotect products table - might be vanilla PostgreSQL: {}", e.getMessage());
            }
        }
        
        for (String operation : request.getOperations()) {
            MultiBenchmarkResult.OperationResult opResult = null;
            
            try {
                switch (operation.toUpperCase()) {
                    case "SELECT":
                        opResult = benchmarkSelect(request.getIterations());
                        break;
                    case "INSERT":
                        opResult = benchmarkInsert(request.getIterations());
                        break;
                    case "UPDATE":
                        opResult = benchmarkUpdate(request.getIterations());
                        break;
                    case "DELETE":
                        opResult = benchmarkDelete(request.getIterations());
                        break;
                    default:
                        opResult = new MultiBenchmarkResult.OperationResult();
                        opResult.setError("Unknown operation: " + operation);
                }
            } catch (Exception e) {
                opResult = new MultiBenchmarkResult.OperationResult();
                opResult.setError("Failed to run " + operation + ": " + e.getMessage());
                log.error("Benchmark failed for operation: " + operation, e);
            }
            
            if (opResult != null) {
                operationResults.put(operation, opResult);
            }
        }
        
        result.setTotalTimeMs((double)(System.nanoTime() - totalStartTime) / 1_000_000.0);
        result.setOperationResults(operationResults);
        
        // Restore protection status if we unprotected the table
        if (needsUnprotection && wasProtected) {
            try {
                protectionService.protectTable("products");
                log.info("Restored protection on products table after benchmarking");
            } catch (Exception e) {
                log.warn("Could not restore protection on products table: {}", e.getMessage());
            }
        }
        
        return result;
    }
    
    private MultiBenchmarkResult.OperationResult benchmarkSelect(int iterations) {
        MultiBenchmarkResult.OperationResult result = new MultiBenchmarkResult.OperationResult();
        result.setIterations(iterations);
        
        // Get min and max product IDs for range
        Long minId = productRepository.findMinId();
        Long maxId = productRepository.findMaxId();
        
        if (minId == null || maxId == null) {
            result.setError("No products found for SELECT benchmark");
            return result;
        }
        
        List<Long> timings = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        // Warm-up phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try {
                Long randomId = ThreadLocalRandom.current().nextLong(minId, maxId + 1);
                productRepository.findById(randomId);
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Actual benchmark
        long totalTimeNanos = 0;
        for (int i = 0; i < iterations; i++) {
            try {
                Long randomId = ThreadLocalRandom.current().nextLong(minId, maxId + 1);
                
                long opStart = System.nanoTime();
                Optional<Product> product = productRepository.findById(randomId);
                long opEnd = System.nanoTime();
                
                timings.add(opEnd - opStart);
                
                if (product.isPresent()) {
                    successCount++;
                } else {
                    // ID might not exist in range, this is expected
                    successCount++;
                }
            } catch (Exception e) {
                errorCount++;
                log.debug("SELECT operation failed: {}", e.getMessage());
            }
        }
        
        // Calculate total time from timings
        totalTimeNanos = timings.stream().mapToLong(Long::longValue).sum();
        
        result.setTotalTimeMs((double) totalTimeNanos / 1_000_000.0);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setBlockedCount(0); // SELECT operations are never blocked
        
        if (!timings.isEmpty()) {
            result.setAvgTimeMs((double) totalTimeNanos / timings.size() / 1_000_000.0);
            result.setOpsPerSecond(timings.size() * 1_000_000_000.0 / totalTimeNanos);
            
            // Calculate percentiles
            Collections.sort(timings);
            result.setP50TimeMs(calculatePercentile(timings, 50));
            result.setP95TimeMs(calculatePercentile(timings, 95));
            result.setP99TimeMs(calculatePercentile(timings, 99));
        }
        
        return result;
    }
    
    @Transactional
    private MultiBenchmarkResult.OperationResult benchmarkInsert(int iterations) {
        MultiBenchmarkResult.OperationResult result = new MultiBenchmarkResult.OperationResult();
        result.setIterations(iterations);
        
        List<Long> timings = new ArrayList<>();
        List<Long> createdIds = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        
        // Warm-up phase
        List<Long> warmupIds = new ArrayList<>();
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try {
                Product product = new Product();
                product.setName("Warmup Product " + System.nanoTime());
                product.setCategory("Warmup");
                product.setPrice(new BigDecimal("1.00"));
                product.setStockQuantity(1);
                product.setDescription("Warmup");
                
                Product saved = productRepository.save(product);
                warmupIds.add(saved.getId());
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Clean up warmup products
        for (Long id : warmupIds) {
            try {
                productRepository.deleteById(id);
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        // Actual benchmark
        long totalTimeNanos = 0;
        for (int i = 0; i < iterations; i++) {
            try {
                Product product = new Product();
                product.setName("Benchmark Product " + System.nanoTime() + "_" + i);
                product.setCategory("Benchmark");
                product.setPrice(new BigDecimal("99.99"));
                product.setStockQuantity(100);
                product.setDescription("Benchmark test product");
                
                long opStart = System.nanoTime();
                Product saved = productRepository.save(product);
                long opEnd = System.nanoTime();
                
                timings.add(opEnd - opStart);
                createdIds.add(saved.getId());
                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.debug("INSERT operation failed: {}", e.getMessage());
            }
        }
        
        // Calculate total time from timings
        totalTimeNanos = timings.stream().mapToLong(Long::longValue).sum();
        
        // Clean up created products (outside of benchmark timing)
        for (Long id : createdIds) {
            try {
                productRepository.deleteById(id);
            } catch (Exception e) {
                log.debug("Failed to cleanup product {}: {}", id, e.getMessage());
            }
        }
        
        result.setTotalTimeMs((double) totalTimeNanos / 1_000_000.0);
        result.setSuccessCount(successCount);
        result.setErrorCount(errorCount);
        result.setBlockedCount(0); // INSERT operations are typically not blocked
        
        if (!timings.isEmpty()) {
            result.setAvgTimeMs((double) totalTimeNanos / timings.size() / 1_000_000.0);
            result.setOpsPerSecond(timings.size() * 1_000_000_000.0 / totalTimeNanos);
            
            // Calculate percentiles
            Collections.sort(timings);
            result.setP50TimeMs(calculatePercentile(timings, 50));
            result.setP95TimeMs(calculatePercentile(timings, 95));
            result.setP99TimeMs(calculatePercentile(timings, 99));
        }
        
        return result;
    }
    
    @Transactional
    private MultiBenchmarkResult.OperationResult benchmarkUpdate(int iterations) {
        MultiBenchmarkResult.OperationResult result = new MultiBenchmarkResult.OperationResult();
        result.setIterations(iterations);
        
        // Get product IDs for updates
        Long minId = productRepository.findMinId();
        Long maxId = productRepository.findMaxId();
        
        if (minId == null || maxId == null) {
            result.setError("No products found for UPDATE benchmark");
            return result;
        }
        
        List<Long> timings = new ArrayList<>();
        int successCount = 0;
        int blockedCount = 0;
        int errorCount = 0;
        
        // Warm-up phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            try {
                Long randomId = ThreadLocalRandom.current().nextLong(minId, maxId + 1);
                Optional<Product> productOpt = productRepository.findById(randomId);
                
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setDescription("Warmup update " + i);
                    productRepository.save(product);
                }
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Actual benchmark
        long totalTimeNanos = 0;
        for (int i = 0; i < iterations; i++) {
            try {
                Long randomId = ThreadLocalRandom.current().nextLong(minId, maxId + 1);
                Optional<Product> productOpt = productRepository.findById(randomId);
                
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    product.setDescription("Updated at " + System.nanoTime() + "_" + i);
                    
                    long opStart = System.nanoTime();
                    productRepository.save(product);
                    long opEnd = System.nanoTime();
                    
                    timings.add(opEnd - opStart);
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
        
        // Calculate total time from timings
        totalTimeNanos = timings.stream().mapToLong(Long::longValue).sum();
        
        result.setTotalTimeMs((double) totalTimeNanos / 1_000_000.0);
        result.setSuccessCount(successCount);
        result.setBlockedCount(blockedCount);
        result.setErrorCount(errorCount);
        
        if (!timings.isEmpty()) {
            result.setAvgTimeMs((double) totalTimeNanos / timings.size() / 1_000_000.0);
            result.setOpsPerSecond(timings.size() * 1_000_000_000.0 / totalTimeNanos);
            
            // Calculate percentiles
            Collections.sort(timings);
            result.setP50TimeMs(calculatePercentile(timings, 50));
            result.setP95TimeMs(calculatePercentile(timings, 95));
            result.setP99TimeMs(calculatePercentile(timings, 99));
        }
        
        return result;
    }
    
    @Transactional
    private MultiBenchmarkResult.OperationResult benchmarkDelete(int iterations) {
        MultiBenchmarkResult.OperationResult result = new MultiBenchmarkResult.OperationResult();
        result.setIterations(iterations);
        
        List<Long> timings = new ArrayList<>();
        int successCount = 0;
        int blockedCount = 0;
        int errorCount = 0;
        
        // Create products to delete (including warmup)
        List<Long> allIds = new ArrayList<>();
        int totalToCreate = iterations + WARMUP_ITERATIONS;
        
        for (int i = 0; i < totalToCreate; i++) {
            try {
                Product product = new Product();
                product.setName("Delete Benchmark " + System.nanoTime() + "_" + i);
                product.setCategory("DeleteBenchmark");
                product.setPrice(new BigDecimal("1.99"));
                product.setStockQuantity(1);
                product.setDescription("To be deleted");
                
                Product saved = productRepository.save(product);
                allIds.add(saved.getId());
            } catch (Exception e) {
                log.debug("Failed to create product for delete benchmark: {}", e.getMessage());
            }
        }
        
        if (allIds.size() < iterations) {
            result.setError("Failed to create enough products for DELETE benchmark");
            return result;
        }
        
        // Warm-up phase - delete first WARMUP_ITERATIONS products
        for (int i = 0; i < Math.min(WARMUP_ITERATIONS, allIds.size()); i++) {
            try {
                productRepository.deleteById(allIds.get(i));
            } catch (Exception e) {
                // Ignore warmup errors
            }
        }
        
        // Actual benchmark - delete remaining products
        long totalTimeNanos = 0;
        for (int i = WARMUP_ITERATIONS; i < Math.min(WARMUP_ITERATIONS + iterations, allIds.size()); i++) {
            try {
                Long id = allIds.get(i);
                
                long opStart = System.nanoTime();
                productRepository.deleteById(id);
                long opEnd = System.nanoTime();
                
                timings.add(opEnd - opStart);
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
        
        // Calculate total time from timings
        totalTimeNanos = timings.stream().mapToLong(Long::longValue).sum();
        
        // Clean up any remaining products
        for (Long id : allIds) {
            try {
                if (productRepository.existsById(id)) {
                    productRepository.deleteById(id);
                }
            } catch (Exception e) {
                // Ignore cleanup errors
            }
        }
        
        result.setTotalTimeMs((double) totalTimeNanos / 1_000_000.0);
        result.setSuccessCount(successCount);
        result.setBlockedCount(blockedCount);
        result.setErrorCount(errorCount);
        
        if (!timings.isEmpty()) {
            result.setAvgTimeMs((double) totalTimeNanos / timings.size() / 1_000_000.0);
            result.setOpsPerSecond(timings.size() * 1_000_000_000.0 / totalTimeNanos);
            
            // Calculate percentiles
            Collections.sort(timings);
            result.setP50TimeMs(calculatePercentile(timings, 50));
            result.setP95TimeMs(calculatePercentile(timings, 95));
            result.setP99TimeMs(calculatePercentile(timings, 99));
        }
        
        return result;
    }
    
    private double calculatePercentile(List<Long> sortedTimings, double percentile) {
        if (sortedTimings.isEmpty()) {
            return 0.0;
        }
        
        int index = (int) Math.ceil(percentile / 100.0 * sortedTimings.size()) - 1;
        index = Math.max(0, Math.min(index, sortedTimings.size() - 1));
        
        // Convert nanoseconds to milliseconds
        return sortedTimings.get(index) / 1_000_000.0;
    }
}