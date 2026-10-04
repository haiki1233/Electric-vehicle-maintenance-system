package com.example.auth_service.dto.request;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullname;
    private String email;
    private String password;
}