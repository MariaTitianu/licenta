package com.mariatitianu.licenta.repository.jdbc;

import com.mariatitianu.licenta.repository.ProtectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("jdbc")
@RequiredArgsConstructor
public class ProtectionJdbcRepository implements ProtectionRepository {
    
    private final DataSource dataSource;
    
    @Override
    public Boolean protectTable(String tableName) {
        // VULNERABLE: Direct string concatenation in function call
        String sql = "SELECT warden_protect('" + tableName + "')";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error protecting table", e);
        }
        
        return false;
    }
    
    @Override
    public Boolean unprotectTable(String tableName) {
        // VULNERABLE: Direct string concatenation in function call
        String sql = "SELECT warden_unprotect('" + tableName + "')";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error unprotecting table", e);
        }
        
        return false;
    }
    
    @Override
    public List<String> getUnprotectedTables() {
        List<String> tables = new ArrayList<>();
        // Direct query - less vulnerable but still using Statement
        String sql = "SELECT table_name FROM public.warden_unprotected_tables ORDER BY table_name";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching unprotected tables", e);
        }
        
        return tables;
    }
    
    @Override
    public List<Object[]> getUnprotectedTablesAlt() {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT * FROM public.warden_unprotected_tables ORDER BY table_name";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                results.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching unprotected tables data", e);
        }
        
        return results;
    }
    
    @Override
    public Integer isTableUnprotected(String tableName) {
        // VULNERABLE: Direct string concatenation in WHERE clause
        String sql = "SELECT COUNT(*) FROM public.warden_unprotected_tables " +
                     "WHERE table_name = '" + tableName + "'";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking table protection status", e);
        }
        
        return 0;
    }
}