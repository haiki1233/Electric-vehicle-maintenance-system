package com.example.catalog_product_service.service;

import com.example.catalog_product_service.dto.request.ProductRequest;
import com.example.catalog_product_service.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Long id);
    ProductResponse createProduct(ProductRequest request);
    ProductResponse updateProduct(Long id, ProductRequest request);
    ProductResponse updateProductStatus(Long id, boolean isActive); // Dùng cho PATCH
    ProductResponse updateProductStock(Long id, Integer stockQuantity);
    void deleteProduct(Long id);
}