package com.postech.fiap.parkingmeter.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/owner")
public class OwnerController {

  @GetMapping
  public ResponseEntity<String> getTest() {
    return ResponseEntity.ok("Hello World");
  }
}
