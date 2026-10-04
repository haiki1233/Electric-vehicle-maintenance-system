package com.example.apigetway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * CẤU HÌNH BẢO MẬT DUY NHẤT CỦA GATEWAY
 *
 * Luồng xác thực (1 chiều, không chồng chéo):
 *   Request
 *     └─► AuthenticationWebFilter (Spring tự thêm khi bật oauth2ResourceServer)
 *           ├─ Đọc header "Authorization: Bearer <token>"
 *           ├─ Verify chữ ký bằng jwtDecoder() (HS256 + secretKey)
 *           ├─ Kiểm tra exp/nbf
 *           ├─ Map claim "role" → authority "ROLE_<role>" qua jwtAuthenticationConverter()
 *           └─ Set Authentication vào ReactiveSecurityContext
 *     └─► authorizeExchange(...)  ← Phân quyền theo path
 *           ├─ permitAll()      → cho qua
 *           ├─ hasRole("ADMIN") → cần authority ROLE_ADMIN
 *           └─ authenticated()  → cần token hợp lệ bất kỳ
 *
 * KHÔNG viết thêm WebFilter xác thực thủ công — sẽ gây xung đột authorities.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(Customizer.withDefaults())
            .authorizeExchange(exchanges -> exchanges
                // 1. PUBLIC — không cần token
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()

                // 2. ADMIN — yêu cầu authority ROLE_ADMIN (được map từ claim "role")
                .pathMatchers(HttpMethod.GET,   "/api/v1/users").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PATCH, "/api/v1/users/*/role").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET,   "/api/v1/roles").hasRole("ADMIN")

                // 3. Còn lại — chỉ cần đăng nhập (token hợp lệ)
                .anyExchange().authenticated()
            )
            // Bật OAuth2 Resource Server: Spring tự thêm AuthenticationWebFilter xử lý Bearer token
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Decoder: verify chữ ký HS256 bằng secret key.
     * Token sai chữ ký / hết hạn → Spring trả 401 tự động, không cần catch thủ công.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    /**
     * Converter: đọc claim "role" trong payload JWT, thêm prefix "ROLE_".
     * Ví dụ: payload {"role":"ADMIN"} → authority "ROLE_ADMIN"
     *        → .hasRole("ADMIN") match ✅
     */
    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter);
    }
}