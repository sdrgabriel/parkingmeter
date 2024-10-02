package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleSpentDTO {

  private String licensePlate;
  private double totalSpent;
}
