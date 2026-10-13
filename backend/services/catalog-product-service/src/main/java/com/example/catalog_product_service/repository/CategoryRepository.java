package com.example.catalog_product_service.repository;

import com.example.catalog_product_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Kiểm tra xem tên danh mục đã tồn tại chưa (Dùng lúc Create/Update)
    boolean existsByName(String name);
    
    // Lấy danh sách các danh mục đang hoạt động (isActive = true)
    List<Category> findByIsActiveTrue();
}