package com.mariatitianu.licenta.repository;

import java.util.List;
import java.util.Map;

public interface LogRepository {
    
    List<Map<String, Object>> getRecentOperations(int limit);
    
    List<Map<String, Object>> getAllOperations();
}