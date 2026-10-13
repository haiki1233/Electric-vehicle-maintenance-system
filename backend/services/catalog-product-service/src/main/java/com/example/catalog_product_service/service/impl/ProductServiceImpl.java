package com.example.catalog_product_service.service.impl;

import com.example.catalog_product_service.dto.request.ProductRequest;
import com.example.catalog_product_service.dto.response.CategoryResponse;
import com.example.catalog_product_service.dto.response.ProductResponse;
import com.example.catalog_product_service.entity.Category;
import com.example.catalog_product_service.entity.Product;
import com.example.catalog_product_service.exception.DuplicateResourceException;
import com.example.catalog_product_service.exception.ResourceNotFoundException;
import com.example.catalog_product_service.repository.CategoryRepository;
import com.example.catalog_product_service.repository.ProductRepository;
import com.example.catalog_product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // --- HÀM HELPER: CHUYỂN ĐỔI ENTITY -> DTO ---
    private ProductResponse mapToResponse(Product product) {
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .description(product.getCategory().getDescription())
                .type(product.getCategory().getType())
                .isActive(product.getCategory().isActive())
                .build();

        return ProductResponse.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.isActive())
                .category(categoryResponse) // Trả về DTO
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse) // Dùng vòng lặp biến tất cả Product thành ProductResponse
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Mã SKU này đã tồn tại trong hệ thống!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại với ID: " + request.getCategoryId()));

        Product newProduct = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
                .category(category)
                .isActive(true)
                .build();

        // Lưu DB xong thì map kết quả sang DTO để trả về
        return mapToResponse(productRepository.save(newProduct));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException("Mã SKU này đã tồn tại trong hệ thống!");
        }

        // LỚP PHÒNG THỦ SÂU: Đảm bảo không ai bypass được Controller
        if (request.getStockQuantity() != null && request.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được nhỏ hơn 0!");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại với ID: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProductStatus(Long id, boolean isActive) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        product.setActive(isActive);
        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm để xóa");
        }
        productRepository.deleteById(id);
    }

    // HÀM MỚI DÀNH CHO PATCH SỐ LƯỢNG TỒN KHO
    @Override
    @Transactional
    public ProductResponse updateProductStock(Long id, Integer stockQuantity) {
        // Chặn ngay lập tức nếu số lượng rỗng hoặc bị âm
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không hợp lệ (phải >= 0)!");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));

        product.setStockQuantity(stockQuantity);
        return mapToResponse(productRepository.save(product));
    }
}