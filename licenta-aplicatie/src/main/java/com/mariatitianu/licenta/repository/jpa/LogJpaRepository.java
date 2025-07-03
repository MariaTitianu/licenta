package com.mariatitianu.licenta.repository.jpa;

import com.mariatitianu.licenta.entity.UnprotectedTable;
import com.mariatitianu.licenta.repository.LogRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Profile("jpa")
public interface LogJpaRepository extends LogRepository, JpaRepository<UnprotectedTable, String> {
    
    @Override
    @Query(value = "SELECT * FROM warden_all_queries() ORDER BY log_timestamp DESC LIMIT :limit", nativeQuery = true)
    List<Map<String, Object>> getRecentOperations(@Param("limit") int limit);
    
    @Override
    @Query(value = "SELECT * FROM warden_all_queries() ORDER BY log_timestamp DESC", nativeQuery = true)
    List<Map<String, Object>> getAllOperations();
}