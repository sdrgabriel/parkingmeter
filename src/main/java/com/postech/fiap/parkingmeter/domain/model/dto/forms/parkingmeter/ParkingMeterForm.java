package com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ParkingMeterForm(
    @JsonAlias("operating_hours") @Valid @NotNull OperatingHoursParkingForm operatingHours,
    @Valid @NotNull RateParkingForm rate,
    @JsonAlias("available_spots") @Min(value = 1) int availableSpots,
    @Valid @NotNull AddressParkingForm address) {}
