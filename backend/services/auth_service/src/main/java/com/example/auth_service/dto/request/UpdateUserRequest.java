package com.example.auth_service.dto.request;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String fullname;
    private String email;
}