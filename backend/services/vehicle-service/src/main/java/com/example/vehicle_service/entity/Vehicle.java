package com.example.vehicle_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // Liên kết mềm tới ID của user bên auth_service

    @Column(nullable = false)
    private String modelName;

    @Column(unique = true, nullable = false)
    private String licensePlate;

    @Column(unique = true, nullable = false)
    private String vinNumber;

    private String batteryType;
    private Double batteryCapacityKwh;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
