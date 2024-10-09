package com.postech.fiap.parkingmeter.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "owner")
@Data
@Builder
public class Owner {

  @Id private String id;

  private String name;

  @Indexed(unique = true)
  private String cpf;

  @Indexed(unique = true)
  private String email;

  private String phone;

  private Address address;
}
