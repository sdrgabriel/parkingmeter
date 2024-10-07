package com.postech.fiap.parkingmeter.domain.model;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Address;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.OperatingHours;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
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
  @Field("horario_funcionamento")
  private OperatingHours horarioFuncionamento;

  @Valid private Tarifa tarifa;

  @Min(value = 1)
  @Indexed
  @Field("vagas_disponiveis")
  private int vagasDisponiveis;

  @Valid @NotNull @Indexed private Address endereco;

  @Version private Long version;
}
