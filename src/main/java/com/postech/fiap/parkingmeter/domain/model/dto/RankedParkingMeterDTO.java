package com.postech.fiap.parkingmeter.domain.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class RankedParkingMeterDTO {

  private String data;
  private List<ParkingMeterArrecadacaoDTO> parquimetros;

  @Data
  public static class ParkingMeterArrecadacaoDTO {
    private String parquimetroId;
    private double totalArrecadado;
  }
}
