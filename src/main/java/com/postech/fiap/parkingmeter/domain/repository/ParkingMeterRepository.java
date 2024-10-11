package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingMeterRepository extends MongoRepository<ParkingMeter, String> {

  boolean existsByAddress_ZipCode(String zipCode);

  @Query(" { '$and': [ {'$or': [{'address.city': ?0},{'null': ?0} ] }, {'$or': [{'address.neighborhood': ?1}, {'null': ?1} ]} ] } ")
  Page<ParkingMeter> findAllByCityAndOrNeighborhood(
      String city, String neighborhood, Pageable pageable);
}
