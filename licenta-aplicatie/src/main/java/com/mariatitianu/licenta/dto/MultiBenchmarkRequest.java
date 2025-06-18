package com.mariatitianu.licenta.dto;

import lombok.Data;
import java.util.List;

@Data
public class MultiBenchmarkRequest {
    private int iterations = 100;
    private List<String> operations; // ["SELECT", "INSERT", "UPDATE", "DELETE"]
}