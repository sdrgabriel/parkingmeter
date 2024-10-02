package com.postech.fiap.parkingmeter.domain.model.parkingmeter;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.mongodb.core.index.Indexed;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Endereco(
    String logradouro,
    String bairro,
    @JsonAlias("localidade") String cidade,
    String estado,
    @Indexed(unique = true)
    String cep) {}
