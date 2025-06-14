package com.mariatitianu.licenta.repository;

import java.util.List;

public interface ProtectionRepository {
    
    Boolean protectTable(String tableName);
    
    Boolean unprotectTable(String tableName);
    
    List<String> getUnprotectedTables();
    
    List<Object[]> getUnprotectedTablesAlt();
    
    Integer isTableUnprotected(String tableName);
}