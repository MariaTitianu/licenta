package com.mariatitianu.licenta.repository;

import com.mariatitianu.licenta.entity.UnprotectedTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnprotectedTableRepository extends JpaRepository<UnprotectedTable, String> {
    
    @Query(value = "SELECT * FROM warden_unprotected_tables", nativeQuery = true)
    List<UnprotectedTable> findAllUnprotectedTables();
    
    @Query(value = "SELECT COUNT(*) FROM information_schema.tables " +
                   "WHERE table_schema = 'public' " +
                   "AND table_type = 'BASE TABLE' " +
                   "AND table_name = :tableName", nativeQuery = true)
    Integer checkTableExists(@Param("tableName") String tableName);
    
    @Query(value = "SELECT COUNT(*) FROM information_schema.tables " +
                   "WHERE table_schema = 'public' " +
                   "AND table_type = 'BASE TABLE' " +
                   "AND table_name NOT LIKE 'pg_%' " +
                   "AND table_name != 'warden_unprotected_tables'", nativeQuery = true)
    Integer countUserTables();
}