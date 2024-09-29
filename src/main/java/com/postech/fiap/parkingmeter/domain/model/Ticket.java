package com.postech.fiap.parkingmeter.domain.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ticket")
@Data
public class Ticket {
  @Id private String id;

  @Valid
  @NotNull
  @Field("valor_total_cobrado")
  private double valorTotalCobrado;

  @Valid
  @NotNull
  @Field("horario_inicio")
  private LocalDateTime horarioInicio;

  @Valid
  @Field("horario_fim")
  private LocalDateTime horarioFim;

  @Valid
  @NotNull
  @Field("status_pagamento")
  private StatusPagamento statusPagamento;

  @Valid
  @NotNull
  private ParkingMeter parquimetro;

  @Valid
  @NotNull
  private Vehicle veiculo;

  public enum StatusPagamento {
    CANCELADO,
    PENDENTE,
    PAGO
  }
}
