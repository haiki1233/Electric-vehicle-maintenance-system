package com.example.auth_service.config;

import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.enums.UserStatus;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Lấy email từ Google
        String email = oAuth2User.getAttribute("email");
        String fullname = oAuth2User.getAttribute("name");

        // 2. Kiểm tra email trong DB
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isEmpty()) {
            Role customerRole = roleRepository.findByName("CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            user = User.builder()
                    .fullname(fullname != null ? fullname : email)
                    .email(email)
                    .password(null)
                    .role(customerRole)
                    .provider("GOOGLE")
                    .status(UserStatus.ACTIVE)
                    .enabled(true)
                    .accountNonLocked(true)
                    .build();
        } else {
            user = userOptional.get();

            // Chặn đăng nhập Google nếu tài khoản bị khóa
            if (user.getStatus() != UserStatus.ACTIVE || !user.isEnabled() || !user.isAccountNonLocked()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản của bạn đã bị khóa!");
                return;
            }
        }

        // Cập nhật thời gian đăng nhập
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 3. Tạo JWT
        String token = jwtUtil.generateToken(email, user.getRole() != null ? user.getRole().getName() : "CUSTOMER");

        // 4. Redirect về Frontend (cổng 3000) kèm token trên URL
        String targetUrl = "http://localhost:3000/login/success?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}