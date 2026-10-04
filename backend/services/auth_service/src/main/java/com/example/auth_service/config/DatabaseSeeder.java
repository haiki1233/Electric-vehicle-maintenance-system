package com.example.auth_service.config;

import com.example.auth_service.entity.Role;
import com.example.auth_service.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DatabaseSeeder {
    @Bean
    public CommandLineRunner initDatabase(RoleRepository roleRepository) {
        return args -> {
            if (roleRepository.count() == 0) {
                roleRepository.saveAll(List.of(
                    Role.builder().name("CUSTOMER").description("Khách hàng").build(),
                    Role.builder().name("ADMIN").description("Quản trị viên").build(),
                    Role.builder().name("TECHNICIAN").description("Kỹ thuật viên").build(),
                    Role.builder().name("RECEPTIONIST").description("Lễ tân").build()
                ));
            }
        };
    }
}