package com.postech.fiap.parkingmeter.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OwnerDTO {

  private String id;
  private String name;
  private String cpf;
  private String phone;
  private String email;
  private AddressDTO address;
}