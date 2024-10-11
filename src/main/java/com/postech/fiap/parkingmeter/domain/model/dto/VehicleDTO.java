package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VehicleDTO {

  private String id;

  @Field("license_plate")
  private String licensePlate;

  private String model;

  private String color;

  private OwnerDTO owner;
}
