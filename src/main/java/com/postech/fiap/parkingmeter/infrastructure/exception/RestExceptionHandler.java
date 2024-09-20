package com.postech.fiap.parkingmeter.infrastructure.exception;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
