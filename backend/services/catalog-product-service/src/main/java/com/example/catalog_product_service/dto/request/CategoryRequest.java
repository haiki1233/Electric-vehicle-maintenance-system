package com.example.catalog_product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;

    @NotBlank(message = "Phân loại (type) không được để trống")
    private String type; // VD: "MAINTENANCE_PACKAGE" hoặc "SPARE_PART"
}