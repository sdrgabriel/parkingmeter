package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingMeterRepository extends MongoRepository<ParkingMeter, String> {
    @Query("{'endereco.cep': ?0 }")
    List<ParkingMeter> findAllByEndereco_Cep(String cep);

    @Query(" { '$and': [ {'$or': [{'endereco.cidade': ?0},{'null': ?0} ] }, {'$or': [{'endereco.bairro': ?1}, {'null': ?1} ]} ] } ")
    Page<ParkingMeter> findAllByCidadeOrBairro(String cidade, String bairro, Pageable pageable);
}
