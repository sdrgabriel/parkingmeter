package com.postech.fiap.parkingmeter.domain.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDTO {

  private String id;
  private double totalAmountCharged;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private String paymentStatus;
  private ParkingMeterDTO parkingMeter;
  private VehicleDTO vehicle;
}
