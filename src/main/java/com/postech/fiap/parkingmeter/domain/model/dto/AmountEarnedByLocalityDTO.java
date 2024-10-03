package com.postech.fiap.parkingmeter.domain.model.dto;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmountEarnedByLocalityDTO {
    private String id;
    private Endereco endereco;
    private Double earned;
}
