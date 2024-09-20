package com.postech.fiap.parkingmeter.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class VehicleException extends Exception {

  private final HttpStatus status;

  public VehicleException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
