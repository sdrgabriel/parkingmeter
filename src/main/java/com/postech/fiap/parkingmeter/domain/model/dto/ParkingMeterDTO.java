package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParkingMeterDTO {

  private String id;
  private OperatingHoursParkingDTO operatingHours;
  private RateDTO rate;
  private int availableSpaces;
  private AddressDTO address;
  private long version;
}
