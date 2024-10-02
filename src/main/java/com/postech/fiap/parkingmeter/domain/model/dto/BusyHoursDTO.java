package com.postech.fiap.parkingmeter.domain.model.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusyHoursDTO {

  private String parquimetroId;
  private LocalDateTime horarioMovimentado;
  private Long totalTickets;
}
