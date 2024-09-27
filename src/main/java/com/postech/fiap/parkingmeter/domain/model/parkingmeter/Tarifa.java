package com.postech.fiap.parkingmeter.domain.model.parkingmeter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Tarifa(
    @JsonAlias("primeira_hora") @DecimalMin(value = "1.0") @Field("primeira_hora")
        double primeiraHora,
    @JsonAlias("demais_horas") @DecimalMin(value = "1.0") @Field("demais_horas")
        double demaisHoras) {}
