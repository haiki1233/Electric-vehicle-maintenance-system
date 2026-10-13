package com.example.catalog_product_service.config;

import com.example.catalog_product_service.client.AuthClient;
import com.example.common.api.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.catalog_product_service.dto.response.AuthUserDto;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthClient authClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Nếu có gắn Token, ta sẽ gọi Auth Service kiểm tra
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                ApiResponse<AuthUserDto> authResponse = authClient.getCurrentUser(authHeader);
                
                if (authResponse.isSuccess() && authResponse.getData() != null) {
                    String username = authResponse.getData().getEmail();
                    String roleName = authResponse.getData().getRole().getName();
                    System.out.println("Role nhận được từ Auth Service: " + roleName); // Xem ở màn hình console

                    String cleanRole = roleName.replace("ROLE_", "").toUpperCase();
                    // Thêm tiền tố ROLE_ để Spring Security nhận diện đúng định dạng
                    List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + cleanRole));
                    
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    
                    // Set thông tin người dùng vào ngữ cảnh bảo mật hiện tại
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token hết hạn hoặc auth-service lỗi -> Bỏ qua, SecurityConfig phía dưới sẽ chặn lại
                System.out.println("Lỗi xác thực Token qua Auth-Service: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}