package com.postech.fiap.parkingmeter.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TicketException extends RuntimeException {

  private final HttpStatus status;

  public TicketException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}
