package com.mariatitianu.licenta.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "warden_unprotected_tables")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnprotectedTable {
    
    @Id
    @Column(name = "table_name")
    private String tableName;
    
    @Column(name = "unprotect_time", nullable = false)
    private LocalDateTime unprotectTime;
    
    @Column(name = "unprotected_by", nullable = false)
    private String unprotectedBy;
}