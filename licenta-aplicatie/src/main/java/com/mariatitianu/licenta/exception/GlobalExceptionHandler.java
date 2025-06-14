package com.mariatitianu.licenta.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(DataAccessException ex) {
        String message = ex.getMessage();
        String rootCause = getRootCauseMessage(ex);
        
        HttpStatus status = determineHttpStatus(rootCause);
        String errorType = determineErrorType(rootCause);
        
        Map<String, Object> errorResponse = Map.of(
            "status", status.value(),
            "error", errorType,
            "message", extractMeaningfulMessage(rootCause),
            "details", Map.of(
                "operation", extractOperation(rootCause),
                "table", extractTableName(rootCause),
                "reason", extractReason(rootCause),
                "dbError", rootCause != null ? rootCause : message
            ),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(status).body(errorResponse);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = Map.of(
            "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "error", "RUNTIME_ERROR",
            "message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> errorResponse = Map.of(
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "INVALID_INPUT",
            "message", ex.getMessage() != null ? ex.getMessage() : "Invalid input provided",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    private String getRootCauseMessage(Throwable ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }
    
    private HttpStatus determineHttpStatus(String errorMessage) {
        if (errorMessage == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("protected") || lowerMessage.contains("blocked")) {
            return HttpStatus.FORBIDDEN;
        } else if (lowerMessage.contains("syntax") || lowerMessage.contains("invalid")) {
            return HttpStatus.BAD_REQUEST;
        } else if (lowerMessage.contains("connection") || lowerMessage.contains("timeout")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (lowerMessage.contains("permission") || lowerMessage.contains("access denied")) {
            return HttpStatus.FORBIDDEN;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    private String determineErrorType(String errorMessage) {
        if (errorMessage == null) {
            return "DATABASE_ERROR";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("protected") || lowerMessage.contains("blocked")) {
            return "TABLE_PROTECTED";
        } else if (lowerMessage.contains("syntax")) {
            return "SQL_SYNTAX_ERROR";
        } else if (lowerMessage.contains("connection")) {
            return "CONNECTION_ERROR";
        } else if (lowerMessage.contains("permission") || lowerMessage.contains("access denied")) {
            return "PERMISSION_DENIED";
        } else {
            return "DATABASE_ERROR";
        }
    }
    
    private String extractMeaningfulMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Database operation failed";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("protected")) {
            return "Operation blocked by database protection";
        } else if (lowerMessage.contains("syntax")) {
            return "Invalid SQL syntax";
        } else if (lowerMessage.contains("connection")) {
            return "Database connection failed";
        } else if (lowerMessage.contains("permission") || lowerMessage.contains("access denied")) {
            return "Access denied - insufficient permissions";
        } else {
            return "Database operation failed";
        }
    }
    
    private String extractOperation(String errorMessage) {
        if (errorMessage == null) {
            return "UNKNOWN";
        }
        
        String lowerMessage = errorMessage.toLowerCase();
        
        if (lowerMessage.contains("delete")) {
            return "DELETE";
        } else if (lowerMessage.contains("update")) {
            return "UPDATE";
        } else if (lowerMessage.contains("insert")) {
            return "INSERT";
        } else if (lowerMessage.contains("alter")) {
            return "ALTER";
        } else if (lowerMessage.contains("drop")) {
            return "DROP";
        } else {
            return "UNKNOWN";
        }
    }
    
    private String extractTableName(String errorMessage) {
        if (errorMessage == null) {
            return "unknown";
        }
        
        String[] commonTables = {"products", "customer_payments", "warden_unprotected_tables"};
        String lowerMessage = errorMessage.toLowerCase();
        
        for (String table : commonTables) {
            if (lowerMessage.contains(table)) {
                return table;
            }
        }
        
        return "unknown";
    }
    
    private String extractReason(String errorMessage) {
        if (errorMessage == null) {
            return "Unknown database error";
        }
        
        if (errorMessage.toLowerCase().contains("protected")) {
            return "Table is protected by pg_warden extension";
        } else {
            return errorMessage;
        }
    }
}