package com.postech.fiap.parkingmeter.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OwnerException extends Exception {

  private final HttpStatus status;

  public OwnerException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
