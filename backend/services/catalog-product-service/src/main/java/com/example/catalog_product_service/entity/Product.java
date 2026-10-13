package com.example.catalog_product_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku; // Mã quản lý kho hoặc mã gói bảo dưỡng (VD: BATTERY-001)

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price; // Dùng BigDecimal chuẩn hóa dữ liệu tiền tệ

    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private Integer stockQuantity; // Số lượng tồn kho (áp dụng cho phụ tùng)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    private boolean isActive = true;
}