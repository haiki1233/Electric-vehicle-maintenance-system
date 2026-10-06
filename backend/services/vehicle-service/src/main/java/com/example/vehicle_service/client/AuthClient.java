package com.example.vehicle_service.client;

import com.example.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

// Tên auth-service phải khớp 100% với tên đăng ký trên Eureka
@FeignClient(name = "auth-service")
public interface AuthClient {
    
    @GetMapping("/api/v1/auth/me")
    ApiResponse<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token);
}