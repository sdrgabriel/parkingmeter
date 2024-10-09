package com.postech.fiap.parkingmeter.domain.model.dto.forms;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehicleForm(
    @JsonAlias("license_plate") @Valid @NotNull String licensePlate,
    @JsonAlias("model") @Valid @NotNull String model,
    @JsonAlias("color") @Valid @NotNull String color,
    @JsonAlias("owner") @Valid @NotNull String ownerId) {}
