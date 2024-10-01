package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingMeterRepository extends MongoRepository<ParkingMeter, String> {
    @Query("{'endereco.cep': ?0 }")
    List<ParkingMeter> findAllByEndereco_Cep(String cep);
}
