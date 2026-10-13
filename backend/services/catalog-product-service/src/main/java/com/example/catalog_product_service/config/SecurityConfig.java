package com.example.catalog_product_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            // Vì là Microservices REST API nên tắt lưu phiên Session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Cấp quyền tự do cho khách xem Danh mục và Sản phẩm
                .requestMatchers(HttpMethod.GET, 
                    "/api/v1/categories", "/api/v1/categories/**", 
                    "/api/v1/products", "/api/v1/products/**").permitAll()
                
                // 2. Chặn toàn bộ hành động Thêm/Sửa/Xóa, chỉ dành cho ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/categories/**", "/api/v1/categories", "/api/v1/products/**", "/api/v1/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/categories/**", "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/categories/**", "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**", "/api/v1/products/**").hasRole("ADMIN")
                
                // Các API khác (nếu có sau này) mặc định phải đăng nhập
                .anyRequest().authenticated()
            )
            // Gắn bộ lọc kiểm tra Token vào trước quá trình xử lý Security
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}