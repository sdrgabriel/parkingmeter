package com.postech.fiap.parkingmeter.infrastructure.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, String>> handleOptimisticLockingFailureException(
      OptimisticLockingFailureException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "Concurrency error");
    response.put("message", "The item has been updated by another user. Please try again!");

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<List<String>> handlerValidationException(
      MethodArgumentNotValidException e) {

    List<String> errors =
        e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .toList();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(ParkingMeterException.class)
  public ResponseEntity<String> parkingMeterException(ParkingMeterException ex) {
    return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<String> httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The request body is malformed or contains invalid data.");
  }

  @ExceptionHandler(VehicleException.class)
  public ResponseEntity<String> vehicleException(VehicleException ex) {
    return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
  }

  @ExceptionHandler(TicketException.class)
  public ResponseEntity<String> ticketException(TicketException ex) {
    return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
  }

  @ExceptionHandler(OwnerException.class)
  public ResponseEntity<String> ownerException(OwnerException ex) {
    return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<String> missingServletRequestParameterException(
          MissingServletRequestParameterException ex) {
    return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<String> methodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    String error = "The value of %s you entered is invalid for the parameter %s.".formatted(ex.getValue(), ex.getName());
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }
}
