package com.example.catalog_product_service.dto.response;
import lombok.Data;

@Data
public class AuthUserDto {
    private String email;
    private AuthRoleDto role;
}
