package com.postech.fiap.parkingmeter.domain.model.dto;

import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Address;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.OperatingHours;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingMeterDTO {

  private String id;
  private OperatingHours horarioFuncionamento;
  private Tarifa tarifa;
  private int vagasDisponiveis;
  private Address endereco;
  private long version;
}
