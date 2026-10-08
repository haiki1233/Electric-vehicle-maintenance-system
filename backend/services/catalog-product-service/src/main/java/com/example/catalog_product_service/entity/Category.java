package com.example.catalog_product_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Phân loại: "MAINTENANCE_PACKAGE" (Gói dịch vụ) hoặc "SPARE_PART" (Phụ tùng)
    @Column(nullable = false)
    private String type; 
    
    private boolean isActive = true;
}