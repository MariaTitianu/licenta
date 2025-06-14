package com.mariatitianu.licenta.repository.jdbc;

import com.mariatitianu.licenta.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Profile("jdbc")
@RequiredArgsConstructor
public class LogJdbcRepository implements LogRepository {
    
    private final DataSource dataSource;
    
    @Override
    public List<Map<String, Object>> getRecentOperations(int limit) {
        // VULNERABLE: Direct integer concatenation (less risky but still vulnerable)
        String sql = "SELECT * FROM warden_all_queries() LIMIT " + limit;
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching recent operations", e);
        }
        
        return results;
    }
    
    @Override
    public List<Map<String, Object>> getAllOperations() {
        String sql = "SELECT * FROM warden_all_queries()";
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all operations", e);
        }
        
        return results;
    }
    
    /**
     * Example of an extremely vulnerable method that could be added
     * to demonstrate ORDER BY injection
     */
    public List<Map<String, Object>> getOperationsSorted(String sortField, String sortOrder) {
        // EXTREMELY VULNERABLE: Direct injection in ORDER BY clause
        String sql = "SELECT * FROM warden_all_queries() ORDER BY " + 
                     sortField + " " + sortOrder;
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching sorted operations", e);
        }
        
        return results;
    }
}