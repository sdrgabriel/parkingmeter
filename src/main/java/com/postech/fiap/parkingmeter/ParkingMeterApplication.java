package com.postech.fiap.parkingmeter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class ParkingMeterApplication {

  public static void main(String[] args) {
    SpringApplication.run(ParkingMeterApplication.class, args);
  }
}
