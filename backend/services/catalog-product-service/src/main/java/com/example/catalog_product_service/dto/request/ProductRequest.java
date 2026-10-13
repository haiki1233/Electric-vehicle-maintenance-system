package com.example.catalog_product_service.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Mã SKU không được để trống")
    private String sku;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá tiền phải lớn hơn 0")
    private BigDecimal price;

    private Integer stockQuantity;

    @NotNull(message = "ID Danh mục (category_id) bắt buộc phải có")
    private Long categoryId;
}