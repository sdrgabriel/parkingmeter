package com.postech.fiap.parkingmeter.domain.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketDTO {

  private String id;
  private double valorTotalCobrado;
  private LocalDateTime horarioInicio;
  private LocalDateTime horarioFim;
  private String statusPagamento;
  private ParkingMeterDTO parquimetro;
  private VehicleDTO veiculo;
}
