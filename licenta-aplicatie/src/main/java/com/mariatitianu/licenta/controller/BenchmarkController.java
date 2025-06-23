package com.mariatitianu.licenta.controller;

import com.mariatitianu.licenta.dto.MultiBenchmarkRequest;
import com.mariatitianu.licenta.dto.MultiBenchmarkResult;
import com.mariatitianu.licenta.service.BenchmarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/benchmark")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class BenchmarkController {
    
    private final BenchmarkService benchmarkService;
    
    @PostMapping("/run")
    public ResponseEntity<MultiBenchmarkResult> runMultiBenchmark(@RequestBody MultiBenchmarkRequest request) {
        log.info("Running multi-operation benchmark: operations={}, iterations={}",
                request.getOperations(), request.getIterations());
        
        try {
            MultiBenchmarkResult result = benchmarkService.runMultiBenchmark(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("Invalid benchmark request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Benchmark failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Benchmark service is ready");
    }
}