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
public class ParkingMeterDTO {

  private String id;

  @Field("operating_hours")
  private OperatingHoursParkingDTO operatingHours;

  private RateDTO rate;

  @Field("available_spaces")
  private int availableSpaces;

  private AddressDTO address;

  private long version;
}
