package com.example.apigetway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    // (Sau này bạn inject class tiện ích giải mã JWT vào đây)
    // @Autowired
    // private JwtUtil jwtUtil;

    @Override
    public Mono filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Lấy token từ Header của request
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // 1. Giải mã và kiểm tra token (Sử dụng thư viện như io.jsonwebtoken)
                // String username = jwtUtil.extractUsername(token);
                
                String username = "user_from_token"; // Giả lập dữ liệu sau khi giải mã

                // 2. Khởi tạo đối tượng xác thực 
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.emptyList()
                );

                // 3. Đưa thông tin xác thực vào Reactive Security Context
                // Điều này báo cho SecurityConfig biết rằng request này mang token hợp lệ
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

            } catch (Exception e) {
                // Nếu token sai hoặc hết hạn, không làm gì cả.
                // Request sẽ đi tiếp vào SecurityConfig trong trạng thái "chưa đăng nhập"
                // và SecurityConfig sẽ tự động chặn lại (trả 401) nếu API đó yêu cầu bảo mật.
            }
        }

        // Nếu không có token, cho request đi tiếp để SecurityConfig tự phân xử theo các quy tắc permitAll() hoặc authenticated()
        return chain.filter(exchange);
    }
}