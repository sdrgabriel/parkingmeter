package com.postech.fiap.parkingmeter.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.postech.fiap.parkingmeter")
public class ParkingMeterApplication {

  public static void main(String[] args) {
    SpringApplication.run(ParkingMeterApplication.class, args);
  }
}
