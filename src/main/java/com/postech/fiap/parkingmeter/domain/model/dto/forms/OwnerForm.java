package com.postech.fiap.parkingmeter.domain.model.dto.forms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.AddressParkingForm;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OwnerForm(
    @NotEmpty String name,
    @NotEmpty String cpf,
    @NotEmpty String phone,
    @NotEmpty String email,
    @NotNull AddressParkingForm address) {}
