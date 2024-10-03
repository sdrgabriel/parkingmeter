package com.postech.fiap.parkingmeter.domain.model.dto;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingSpaceDTO {
    private LocalDateTime date;
    private Endereco endereco;
    private Integer spaces;
    private Integer available;
}
