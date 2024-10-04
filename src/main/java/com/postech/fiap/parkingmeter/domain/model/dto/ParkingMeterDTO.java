package com.postech.fiap.parkingmeter.domain.model.dto;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingMeterDTO {

  private String id;
  private HorarioFuncionamento horarioFuncionamento;
  private Tarifa tarifa;
  private int vagasDisponiveis;
  private Endereco endereco;
  private long version;
}
