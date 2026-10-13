package com.example.catalog_product_service.service.impl;

import com.example.catalog_product_service.dto.request.CategoryRequest;
import com.example.catalog_product_service.dto.response.CategoryResponse;
import com.example.catalog_product_service.entity.Category;
import com.example.catalog_product_service.exception.DuplicateResourceException;
import com.example.catalog_product_service.exception.ResourceNotFoundException;
import com.example.catalog_product_service.repository.CategoryRepository;
import com.example.catalog_product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    // --- HÀM HELPER: CHUYỂN ĐỔI ENTITY -> DTO ---
    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .type(category.getType())
                .isActive(category.isActive())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse) // Biến List<Category> thành List<CategoryResponse>
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // Chặn trùng tên danh mục
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tên danh mục này đã tồn tại trong hệ thống!");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .isActive(true)
                .build();
        
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra xem người dùng có đổi tên không, và tên mới đã bị ai lấy chưa
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Tên danh mục này đã tồn tại trong hệ thống!");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setType(request.getType());
        
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục để xóa"));
        
        // Soft delete (Xóa mềm để bảo toàn dữ liệu Product đang dùng danh mục này)
        category.setActive(false);
        categoryRepository.save(category);
    }
}