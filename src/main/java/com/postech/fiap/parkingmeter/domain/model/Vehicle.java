package com.postech.fiap.parkingmeter.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "vehicle")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Vehicle {

  @Id private String id;

  @Field("license_plate")
  private String licensePlate;

  @Field("model")
  private String model;

  @Field("color")
  private String color;

  private Owner owner;
}
