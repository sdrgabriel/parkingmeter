package com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Builder;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record RateParkingForm(
    @JsonAlias("first_hour") @DecimalMin(value = "1.0") @Field("first_hour") double firstHour,
    @JsonAlias("additional_hours") @DecimalMin(value = "1.0") @Field("additional_hours")
        double additionalHours) {}
