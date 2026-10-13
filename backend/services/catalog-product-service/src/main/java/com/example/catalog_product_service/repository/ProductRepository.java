package com.example.catalog_product_service.repository;

import com.example.catalog_product_service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Kiểm tra mã SKU đã tồn tại chưa (Bắt buộc dùng trước khi lưu Sản phẩm mới)
    boolean existsBySku(String sku);

    // Lấy toàn bộ sản phẩm thuộc về một danh mục cụ thể (Dùng khi KH bấm vào Menu danh mục)
    List<Product> findByCategoryId(Long categoryId);

    // Lấy danh sách các sản phẩm đang được bán (isActive = true)
    List<Product> findByIsActiveTrue();
}