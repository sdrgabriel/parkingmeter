package com.postech.fiap.parkingmeter.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class Address {

  private String street;

  private String neighborhood;

  private String city;

  private String state;

  @Indexed(unique = true)
  private String zipCode;

  private String number;

  private String complement;
}
