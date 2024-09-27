package com.postech.fiap.parkingmeter.domain.model.dto;

public record VehicleDTO(
    String id, String licensePlate, String model, String color, OwnerDTO owner) {}
