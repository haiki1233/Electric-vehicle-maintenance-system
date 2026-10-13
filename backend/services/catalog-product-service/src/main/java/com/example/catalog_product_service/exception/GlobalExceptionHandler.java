package com.example.catalog_product_service.exception;

import com.example.common.api.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Tóm lỗi Validation (@Valid, @NotNull, @DecimalMin...)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Lấy tin nhắn lỗi đầu tiên (ví dụ: "Giá tiền phải lớn hơn 0")
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .success(false)
                .message(errorMessage)
                .build());
    }

    // 2. Tóm lỗi Database Constraint (Trùng lặp Unique Key, Vi phạm Khóa ngoại)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Dữ liệu không hợp lệ hoặc đã tồn tại trong hệ thống.";
        
        // Bắt chính xác lỗi cố xóa Danh mục đang chứa Sản phẩm
        if (ex.getMessage() != null && ex.getMessage().contains("violates foreign key constraint")) {
            message = "Không thể xóa! Dữ liệu này đang được sử dụng ở một nơi khác.";
        }
        // Bắt lỗi trùng unique (VD: trùng mã SKU do 2 request gửi cùng lúc)
        else if (ex.getMessage() != null && ex.getMessage().contains("duplicate key value")) {
            message = "Dữ liệu này đã tồn tại trong hệ thống (Trùng mã/Tên).";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.builder()
                .success(false)
                .message(message)
                .build());
    }

    // 3. Tóm lỗi Not Found (Tìm không thấy Danh mục / Sản phẩm)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build());
    }

    // 4. Tóm mọi lỗi Server nội bộ (Code văng NullPointer, Lỗi toán học...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        // Ghi log lỗi ra console để dev fix, nhưng trả về cho khách câu báo lỗi thân thiện
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.builder()
                .success(false)
                .message("Đã xảy ra lỗi hệ thống, vui lòng thử lại sau!")
                .build());
    }

    // 5. Tóm lỗi trùng lặp dữ liệu (Custom Exception)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage()) // <--- Lấy đúng câu chữ tiếng Việt truyền vào
                .build());
    }

    // Bắt các lỗi logic nghiệp vụ (Ví dụ: Tồn kho âm, dữ liệu truyền vào sai)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .build());
    }
}