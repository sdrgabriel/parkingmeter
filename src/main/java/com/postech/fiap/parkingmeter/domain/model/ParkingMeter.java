package com.postech.fiap.parkingmeter.domain.model;

import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.RateParkingForm;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "parkingmeter")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParkingMeter {

  @Id private String id;

  @Valid
  @NotNull
  @Indexed
  @Field("operating_hours")
  private OperationHours operatingHours;

  @Valid private Rate rate;

  @Min(value = 1)
  @Indexed
  @Field("available_spaces")
  private int availableSpaces;

  @Valid @NotNull @Indexed private Address address;

  @Version private Long version;
}
