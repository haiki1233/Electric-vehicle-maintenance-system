package com.example.vehicle_service.controller;

import com.example.common.api.ApiResponse;
import com.example.vehicle_service.client.AuthClient;
import com.example.vehicle_service.dto.request.CreateVehicleRequest;
import com.example.vehicle_service.dto.request.VehicleRequest;
import com.example.vehicle_service.entity.Vehicle;
import com.example.vehicle_service.repository.VehicleRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.vehicle_service.exception.AuthException;
import feign.FeignException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {

    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private AuthClient authClient; // Gọi sang auth_service

    // Helper: Trích xuất User ID từ Token thông qua Feign Client
    private Long getUserIdFromToken(String token) {
        try {
            ApiResponse<Map<String, Object>> response = authClient.getCurrentUser(token);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return Long.valueOf(response.getData().get("id").toString());
            }
            throw new AuthException("Thông tin token không hợp lệ hoặc không có quyền truy cập.");
        } catch (FeignException.Unauthorized e) {
            // Lỗi 401 do auth_service trả về khi token hết hạn
            throw new AuthException("Token đã hết hạn hoặc sai định dạng.");
        } 
        // Lưu ý: Các lỗi Feign khác (500, 503 do sập mạng) sẽ tự động lọt qua đây 
        // và rơi thẳng vào bẫy handleFeignException ở Bước 2.
    }

    // 1. Lấy danh sách xe của cá nhân (CUSTOMER)
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyVehicles(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        
        Long userId = getUserIdFromToken(token);
        if (userId == null) return buildError(HttpStatus.UNAUTHORIZED, "Xác thực thất bại");

        Page<Vehicle> vehiclePage = vehicleRepository.findByUserId(userId, PageRequest.of(page - 1, limit));
        
        Map<String, Object> data = Map.of(
                "items", vehiclePage.getContent(),
                "pagination", Map.of("page", page, "limit", limit, "total_items", vehiclePage.getTotalElements())
        );
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Thành công").data(data).build());
    }

    // 2. Đăng ký xe máy điện mới
    @PostMapping
    public ResponseEntity<ApiResponse> createVehicle(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody CreateVehicleRequest request) {

        Long userId = getUserIdFromToken(token);

        if (vehicleRepository.existsByLicensePlate(request.getLicense_plate())) {
            return buildError(HttpStatus.BAD_REQUEST, "Biển số xe đã tồn tại");
        }
        if (vehicleRepository.existsByVinNumber(request.getVin_number())) {
            return buildError(HttpStatus.BAD_REQUEST, "Số VIN đã tồn tại");
        }

        Vehicle vehicle = Vehicle.builder()
                .userId(userId)
                .modelName(request.getModel_name())
                .licensePlate(request.getLicense_plate())
                .vinNumber(request.getVin_number())
                .batteryType(request.getBattery_type())
                .batteryCapacityKwh(request.getBattery_capacity_kwh())
                .build();

        vehicleRepository.save(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.builder().success(true).message("Đăng ký xe thành công").data(vehicle).build());
    }

    // 3. Xem chi tiết thông tin xe
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getVehicleById(@PathVariable Long id) {
        Optional<Vehicle> vehicle = vehicleRepository.findById(id);
        if (vehicle.isEmpty()) return buildError(HttpStatus.NOT_FOUND, "Không tìm thấy xe");

        return ResponseEntity.ok(ApiResponse.builder().success(true).data(vehicle.get()).build());
    }

    // 4. Cập nhật thông tin xe
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequest request) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(id);
        if (vehicleOpt.isEmpty()) return buildError(HttpStatus.NOT_FOUND, "Không tìm thấy xe");

        Vehicle vehicle = vehicleOpt.get();
        // Kiểm tra trùng biển số nếu có thay đổi
        if (!vehicle.getLicensePlate().equals(request.getLicense_plate()) && vehicleRepository.existsByLicensePlate(request.getLicense_plate())) {
            return buildError(HttpStatus.BAD_REQUEST, "Biển số xe đã tồn tại");
        }

        // Kiểm tra trùng Số VIN nếu có thay đổi Số VIN mới
        if (!vehicle.getVinNumber().equals(request.getVin_number()) && vehicleRepository.existsByVinNumber(request.getVin_number())) {
            return buildError(HttpStatus.BAD_REQUEST, "Số VIN đã tồn tại");
        }

        vehicle.setModelName(request.getModel_name());
        vehicle.setLicensePlate(request.getLicense_plate());
        vehicle.setVinNumber(request.getVin_number());
        vehicle.setBatteryType(request.getBattery_type());
        vehicle.setBatteryCapacityKwh(request.getBattery_capacity_kwh());

        vehicleRepository.save(vehicle);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Cập nhật thành công").data(vehicle).build());
    }

    // 5. Xóa xe
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteVehicle(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) return buildError(HttpStatus.NOT_FOUND, "Không tìm thấy xe");
        
        vehicleRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.builder().success(true).message("Xóa xe thành công").build());
    }

    private ResponseEntity<ApiResponse> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.builder().success(false).message(message).build());
    }
}