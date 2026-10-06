package com.example.vehicle_service.repository;

import com.example.vehicle_service.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Page<Vehicle> findByUserId(Long userId, Pageable pageable);
    
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByVinNumber(String vinNumber);
}