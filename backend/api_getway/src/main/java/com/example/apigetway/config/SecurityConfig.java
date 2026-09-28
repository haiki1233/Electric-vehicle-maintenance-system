package com.example.apigetway.config;


import com.example.apigetway.filter.JwtAuthenticationFilter;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebFluxSecurity 
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(Customizer.withDefaults())
            .authorizeExchange(exchanges -> exchanges
                // BẮT BUỘC THÊM DÒNG NÀY: Mở cửa cho mọi request OPTIONS (CORS Preflight)
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 1. Nhóm Auth: Đăng nhập, đăng ký, cấp lại token (Mở toàn bộ)
                .pathMatchers("/api/auth/**").permitAll()
                
                // 2. Nhóm Xem dữ liệu (Chỉ cho phép HTTP GET): Xem sản phẩm, danh mục, tin tức
                .pathMatchers(HttpMethod.GET, 
                    "/api/products/**", 
                    "/api/categories/**",
                    "/api/services/packages"
                ).permitAll()

                // 3. Nhóm Tra cứu (Chỉ cho phép HTTP GET): Tra cứu bảo hành, trạng thái bảo dưỡng qua mã code
                .pathMatchers(HttpMethod.GET, 
                    "/api/lookup/**",
                    "/api/maintenance/status/**"
                ).permitAll()

                // 4. Nhóm Tương tác mở (Cho phép mọi method): Chatbot, gửi form liên hệ
                .pathMatchers(
                    "/api/chatbot/**",
                    "/api/contact/**"
                ).permitAll()
                
                // 5. Toàn bộ các API còn lại (Đặt lịch, thanh toán, hồ sơ cá nhân...) BẮT BUỘC ĐĂNG NHẬP
                .anyExchange().authenticated()
            )    
            // Chèn JWT Filter vào vị trí xử lý xác thực
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}