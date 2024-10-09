package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleSpentDTO {

  private String licensePlate;
  private double totalSpent;
}
