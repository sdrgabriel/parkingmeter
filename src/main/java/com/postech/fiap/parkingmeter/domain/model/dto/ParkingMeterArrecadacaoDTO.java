package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingMeterArrecadacaoDTO {
    private List<ParkingMeterDTO> parquimetroDetails;
    private double totalArrecadado;
}
