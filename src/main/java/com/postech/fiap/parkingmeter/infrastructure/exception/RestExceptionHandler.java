package com.postech.fiap.parkingmeter.infrastructure.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
