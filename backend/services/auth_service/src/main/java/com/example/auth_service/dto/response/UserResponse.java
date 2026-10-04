package com.example.auth_service.dto.response;
import com.example.auth_service.entity.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String fullname;
    private String email;
    private Role role;
}