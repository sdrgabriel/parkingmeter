package com.postech.fiap.parkingmeter.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class Rate {

  private double firstHour;
  private double additionalHour;
}
