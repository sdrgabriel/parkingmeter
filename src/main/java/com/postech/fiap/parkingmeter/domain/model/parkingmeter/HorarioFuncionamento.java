package com.postech.fiap.parkingmeter.domain.model.parkingmeter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record HorarioFuncionamento(
    @NotNull @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in the format HH:mm")
        String inicio,
    @NotNull @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in the format HH:mm")
        String fim) {}
