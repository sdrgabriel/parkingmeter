package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BusyHoursDTO {

  private ParkingMeterDTO parkingMeter;
  private Integer operatedTime;
  private long totalTickets;
}
