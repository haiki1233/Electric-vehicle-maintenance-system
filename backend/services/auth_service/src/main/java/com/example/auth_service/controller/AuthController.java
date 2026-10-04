package com.example.auth_service.controller;

import com.example.auth_service.dto.request.LoginRequest;
import com.example.auth_service.dto.request.RegisterRequest;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.enums.UserStatus;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.util.JwtUtil;
import com.example.common.api.ApiError;
import com.example.common.api.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Email và mật khẩu không được để trống", "VALIDATION_ERROR");
        }

        String cleanEmail = request.getEmail().trim();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống", "EMAIL_ALREADY_EXISTS");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER").orElseThrow(() -> new RuntimeException("Role not found"));

        User newUser = User.builder()
                .fullname(request.getFullname() != null ? request.getFullname().trim() : "")
                .email(cleanEmail)
                .password(passwordEncoder.encode(request.getPassword().trim()))
                .role(customerRole)
                .provider("LOCAL")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder()
                .success(true)
                .message("Đăng ký tài khoản thành công")
                .data(Map.of("user", mapToUserResponse(newUser)))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        String cleanEmail = request.getEmail().trim();
        Optional<User> userOptional = userRepository.findByEmail(cleanEmail);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getStatus() != UserStatus.ACTIVE || !user.isEnabled() || !user.isAccountNonLocked()) {
                return buildErrorResponse(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa!", "ACCOUNT_LOCKED");
            }

            if (passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);

                String token = jwtUtil.generateToken(user.getEmail(), user.getRole().getName());

                Map data = Map.of(
                        "token_type", "Bearer",
                        "access_token", token,
                        "expires_in", 86400,
                        "user", mapToUserResponse(user)
                );

                return ResponseEntity.ok(ApiResponse.builder().success(true).message("Đăng nhập thành công").data(data).build());
            }
        }
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác", "INVALID_CREDENTIALS");
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        String token = jwtUtil.resolveToken(bearerToken);
        if (token == null) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Không tìm thấy token xác thực", "MISSING_TOKEN");
        }

        try {
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                return ResponseEntity.ok(ApiResponse.builder()
                        .success(true)
                        .message("Lấy thông tin tài khoản thành công")
                        .data(mapToUserResponse(userOptional.get()))
                        .build());
            }
            return buildErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin người dùng", "USER_NOT_FOUND");
        } catch (Exception e) {
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn", "INVALID_TOKEN");
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder().id(user.getId()).fullname(user.getFullname()).email(user.getEmail()).role(user.getRole()).build();
    }

    private ResponseEntity<ApiResponse> buildErrorResponse(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status).body(ApiResponse.builder()
            .success(false)
            .message(message)
            .error(ApiError.create(errorCode, message, null,
                ServletUriComponentsBuilder.fromCurrentRequest().build().getPath()))
            .build());
    }
}