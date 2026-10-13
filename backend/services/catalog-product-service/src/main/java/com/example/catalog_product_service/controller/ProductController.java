package com.example.catalog_product_service.controller;

import com.example.catalog_product_service.dto.request.ProductRequest;
import com.example.catalog_product_service.service.ProductService;
import com.example.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.catalog_product_service.dto.response.ProductResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor // Inject ProductService tự động
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(products).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.builder().success(true).data(product).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        ProductResponse newProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.builder().success(true).message("Tạo sản phẩm thành công").data(newProduct).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        ProductResponse updatedProduct = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true).message("Cập nhật sản phẩm thành công").data(updatedProduct).build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse> updateProductStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        if (!body.containsKey("is_active")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder().success(false).message("Thiếu trường is_active trong body").build());
        }
        
        ProductResponse updatedProduct = productService.updateProductStatus(id, body.get("is_active"));
        String message = updatedProduct.isActive() ? "Đã kích hoạt sản phẩm" : "Đã vô hiệu hóa sản phẩm";
        return ResponseEntity.ok(ApiResponse.builder().success(true).message(message).data(updatedProduct).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Xóa sản phẩm thành công").build());
    }

    // PATCH: Cập nhật riêng số lượng tồn kho
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse> updateProductStock(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> body) {
        if (!body.containsKey("stock_quantity")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.builder().success(false).message("Thiếu trường stock_quantity trong body").build());
        }

        Integer newStock = body.get("stock_quantity");
        ProductResponse updatedProduct = productService.updateProductStock(id, newStock);
        
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Cập nhật tồn kho thành công")
                .data(updatedProduct)
                .build());
    }
}