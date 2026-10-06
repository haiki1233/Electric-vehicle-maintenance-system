package com.example.vehicle_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateVehicleRequest {

    @NotBlank(message = "Tên dòng xe (model_name) không được để trống")
    private String model_name;

    @NotBlank(message = "Biển số xe (license_plate) không được để trống")
    private String license_plate;

    @NotBlank(message = "Số VIN (vin_number) không được để trống")
    private String vin_number;

    @NotBlank(message = "Loại pin (battery_type) không được để trống")
    private String battery_type;

    @NotNull(message = "Dung lượng pin (battery_capacity_kwh) không được để trống")
    @Positive(message = "Dung lượng pin phải lớn hơn 0")
    private Double battery_capacity_kwh;
}