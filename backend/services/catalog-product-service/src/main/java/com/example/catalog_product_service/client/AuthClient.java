package com.example.catalog_product_service.client;

import com.example.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import com.example.catalog_product_service.dto.response.AuthUserDto;


@FeignClient(name = "auth-service")
public interface AuthClient {
    
    // Gọi sang Auth Service để lấy thông tin user dựa vào Token
    @GetMapping("/api/v1/auth/me")
    ApiResponse<AuthUserDto> getCurrentUser(@RequestHeader("Authorization") String token);
}