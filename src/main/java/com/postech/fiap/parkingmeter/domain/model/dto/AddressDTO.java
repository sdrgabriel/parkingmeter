package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressDTO {

  private String street;
  private String neighborhood;
  private String city;
  private String state;
  private String zipCode;
  private String number;
  private String complement;
}
