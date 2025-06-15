package com.mariatitianu.licenta.dto;

import lombok.Data;

@Data
public class BenchmarkRequest {
    private String operation; // "select", "insert", "update", "delete", "all"
    private int iterations = 1000; // Number of operations to perform
    private String tableName = "products"; // Target table
    private String protectionState = "skip"; // "protected", "unprotected", "skip"
}