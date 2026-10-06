package com.example.vehicle_service.dto.request;

import lombok.Data;

@Data
public class VehicleRequest {
    private String model_name;
    private String license_plate;
    private String vin_number;
    private String battery_type;
    private Double battery_capacity_kwh;
}