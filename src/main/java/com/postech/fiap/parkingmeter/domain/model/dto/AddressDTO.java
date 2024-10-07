package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.Builder;

@Builder
public record AddressDTO(
    String logradouro,
    String bairro,
    String cidade,
    String estado,
    String cep,
    Integer numero,
    String complemento) {}
