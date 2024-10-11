package com.postech.fiap.parkingmeter.domain.model.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TicketDTO {

  private String id;

  @Field("total_amount_charged")
  private double totalAmountCharged;

  @Field("start_time")
  private LocalDateTime startTime;

  @Field("end_time")
  private LocalDateTime endTime;

  @Field("payment_status")
  private String paymentStatus;

  private ParkingMeterDTO parkingMeter;

  private VehicleDTO vehicle;
}
