package com.example.vehicle_service.exception;

import com.example.common.api.ApiResponse;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Tóm lỗi Xác thực Token
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse> handleAuthException(AuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error(Map.of("code", "UNAUTHORIZED"))
                .build());
    }

    // 2. Tóm lỗi Validation (Do @NotBlank, @NotNull ở Request DTO)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.builder()
                .success(false)
                .message(errorMessage)
                .error(Map.of("code", "VALIDATION_ERROR"))
                .build());
    }

    // 3. Tóm lỗi Feign khi gọi sang auth_service bị đứt mạng hoặc server sập
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse> handleFeignException(FeignException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ApiResponse.builder()
                .success(false)
                .message("Hệ thống xác thực hiện đang gián đoạn, vui lòng thử lại sau!")
                .error(Map.of("code", "AUTH_SERVICE_UNAVAILABLE"))
                .build());
    }
}