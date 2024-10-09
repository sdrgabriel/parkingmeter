package com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record OperatingHoursParkingForm(
    @NotNull @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in the format HH:mm")
        String start,
    @NotNull @Pattern(regexp = "\\d{2}:\\d{2}", message = "Time must be in the format HH:mm")
        String end) {}
