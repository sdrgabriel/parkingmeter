package com.postech.fiap.parkingmeter.domain.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Builder
public class Endereco {

  private String logradouro;
  private String bairro;
  private String cidade;
  private String estado;
  private String cep;
  private Integer numero;
  private String complemento;
}
