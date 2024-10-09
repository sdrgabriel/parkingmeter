package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDTO {

  private String id;
  private String licensePlate;
  private String model;
  private String color;
  private OwnerDTO owner;
}
