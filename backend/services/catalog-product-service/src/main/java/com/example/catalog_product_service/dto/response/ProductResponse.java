package com.example.catalog_product_service.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private boolean isActive;
    
    // Gắn CategoryResponse vào đây, thay vì dùng Entity Category
    private CategoryResponse category; 
}