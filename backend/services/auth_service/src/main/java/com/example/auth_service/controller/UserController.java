package com.example.auth_service.controller;

import com.example.auth_service.dto.request.UpdateRoleRequest;
import com.example.auth_service.dto.request.UpdateUserRequest;
import com.example.auth_service.dto.response.UserResponse;
import com.example.auth_service.entity.Role;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.common.api.ApiError;
import com.example.common.api.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    // 1. GET /api/v1/users (Danh sách có phân trang & tìm kiếm)
    @GetMapping
    public ResponseEntity<ApiResponse> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long role_id,
            @RequestParam(defaultValue = "") String search) {

        // Pageable của Spring Boot bắt đầu từ index 0, API của frontend thường bắt đầu từ 1
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<User> userPage = userRepository.searchUsers(role_id, search, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        Map<String, Object> data = Map.of(
                "items", userResponses,
                "pagination", Map.of(
                        "page", page,
                        "limit", limit,
                        "total_items", userPage.getTotalElements(),
                        "total_pages", userPage.getTotalPages()
                )
        );

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Lấy danh sách người dùng thành công").data(data).build());
    }

    // 2. GET /api/v1/users/{id} (Xem chi tiết)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) return buildErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng", "USER_NOT_FOUND");

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Lấy thông tin chi tiết thành công").data(mapToUserResponse(user.get())).build());
    }

    // 3. PUT /api/v1/users/{id} (Sửa hồ sơ cá nhân)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return buildErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng", "USER_NOT_FOUND");

        User user = userOptional.get();

        // Kiểm tra email trùng nếu user muốn đổi email
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && !user.getEmail().equals(request.getEmail().trim())) {
            if (userRepository.findByEmail(request.getEmail().trim()).isPresent()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Email đã được sử dụng bởi tài khoản khác", "EMAIL_ALREADY_EXISTS");
            }
            user.setEmail(request.getEmail().trim());
        }

        if (request.getFullname() != null) user.setFullname(request.getFullname().trim());

        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Cập nhật hồ sơ thành công").data(mapToUserResponse(user)).build());
    }

    // 4. PATCH /api/v1/users/{id}/role (Thăng cấp tài khoản)
    @PatchMapping("/{id}/role")
    public ResponseEntity<ApiResponse> updateUserRole(@PathVariable Long id, @RequestBody UpdateRoleRequest request) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) return buildErrorResponse(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng", "USER_NOT_FOUND");

        Optional<Role> roleOptional = roleRepository.findById(request.getRole_id());
        if (roleOptional.isEmpty()) return buildErrorResponse(HttpStatus.BAD_REQUEST, "Role ID không hợp lệ", "INVALID_ROLE");

        User user = userOptional.get();
        user.setRole(roleOptional.get());
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Cập nhật vai trò thành công").data(mapToUserResponse(user)).build());
    }

    // Mapper Helper
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder().id(user.getId()).fullname(user.getFullname()).email(user.getEmail()).role(user.getRole()).build();
    }

    // Error Wrapper Helper
    private ResponseEntity<ApiResponse> buildErrorResponse(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status).body(ApiResponse.builder()
            .success(false)
            .message(message)
            .error(ApiError.create(errorCode, message, null,
                ServletUriComponentsBuilder.fromCurrentRequest().build().getPath()))
            .build());
    }
}