package com.postech.fiap.parkingmeter.domain.model.dto.forms;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.OperatingHours;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParkingMeterForm(
    @JsonAlias("horario_funcionamento") @Valid @NotNull OperatingHours horarioFuncionamento,
    @Valid @NotNull Tarifa tarifa,
    @JsonAlias("vagas_disponiveis") @Min(value = 1) int vagasDisponiveis,
    @NotEmpty String cep) {}
