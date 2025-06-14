package com.mariatitianu.licenta.repository.jpa;

import com.mariatitianu.licenta.entity.UnprotectedTable;
import com.mariatitianu.licenta.repository.ProtectionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Profile("jpa")
public interface ProtectionJpaRepository extends ProtectionRepository, JpaRepository<UnprotectedTable, String> {
    
    @Override
    @Query(value = "SELECT warden_protect(:tableName)", nativeQuery = true)
    Boolean protectTable(@Param("tableName") String tableName);
    
    @Override
    @Query(value = "SELECT warden_unprotect(:tableName)", nativeQuery = true)
    Boolean unprotectTable(@Param("tableName") String tableName);
    
    @Override
    @Query(value = "SELECT table_name FROM public.warden_unprotected_tables ORDER BY table_name", nativeQuery = true)
    List<String> getUnprotectedTables();
    
    @Override
    @Query(value = "SELECT * FROM public.warden_unprotected_tables ORDER BY table_name", nativeQuery = true)
    List<Object[]> getUnprotectedTablesAlt();
    
    @Override
    @Query(value = "SELECT COUNT(*) FROM public.warden_unprotected_tables WHERE table_name = :tableName", nativeQuery = true)
    Integer isTableUnprotected(@Param("tableName") String tableName);
}