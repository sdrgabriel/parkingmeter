package com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter;

import jakarta.validation.constraints.NotNull;

public record AddressParkingForm(
    @NotNull String street,
    @NotNull String neighborhood,
    @NotNull String city,
    @NotNull String state,
    @NotNull String zipCode,
    @NotNull String number,
    @NotNull String complement) {}
