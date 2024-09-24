package com.postech.fiap.parkingmeter.domain.model;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "parkingmeter")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingMeter {
  @Id private String id;

  @Valid
  @NotNull
  @Field("horario_funcionamento")
  private HorarioFuncionamento horarioFuncionamento;

  @Valid private Tarifa tarifa;

  @Min(value = 1)
  @Field("vagas_disponiveis")
  private int vagasDisponiveis;

  @Valid @NotNull private Endereco endereco;

  @Version private Long version;

  public ParkingMeter(
      HorarioFuncionamento horarioFuncionamento,
      Tarifa tarifa,
      int vagasDisponiveis,
      Endereco endereco) {
    this(null, horarioFuncionamento, tarifa, vagasDisponiveis, endereco, null);
  }
}
