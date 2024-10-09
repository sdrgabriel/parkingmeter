package com.postech.fiap.parkingmeter.domain.model.dto.forms;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;

import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketForm(
    @JsonAlias("vehicle_id") @Valid @NotNull String vehicleId,
    @JsonAlias("parking_meter_id") @Valid @NotNull String parkingMeterId) {}
