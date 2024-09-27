package com.postech.fiap.parkingmeter.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.HorarioFuncionamento;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Tarifa;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Pageable;

@DataMongoTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ParkingMeterRepositoryTests {

  private final String ID = new ObjectId("1".repeat(24)).toString();

  @Autowired private ParkingMeterRepository parkingMeterRepository;

  @Test
  @Order(1)
  void create() {
    var parkingMeter =
        new ParkingMeter(
            ID,
            new HorarioFuncionamento("05:00", "15:00"),
            new Tarifa(5, 10),
            7,
            new Endereco(
                "Rua Domingos Barreto", "Jardim Everest", "São Paulo", "São Paulo", "05601-030"),
            null);
    var created = this.parkingMeterRepository.save(parkingMeter);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isEqualTo(ID);
    assertThat(created.getVersion()).isEqualTo(0L);
  }

  @Test
  @Order(2)
  void getById() {
    var get = this.parkingMeterRepository.findById(ID).orElse(null);

    assertThat(get).isNotNull();
    assertThat(get.getId()).isEqualTo(ID);
  }

  @Test
  @Order(3)
  void findAll() {
    var all = this.parkingMeterRepository.findAll(Pageable.unpaged());

    assertThat(all).isNotNull();
    assertThat(all.getTotalElements()).isGreaterThanOrEqualTo(1L);
    assertThat(all.getContent().get(0)).isInstanceOf(ParkingMeter.class);
  }

  @Test
  @Order(4)
  void updateById() {
    var newTarifa = new Tarifa(10, 7);

    var parkingMeterMod = this.parkingMeterRepository.findById(ID).orElseThrow();
    parkingMeterMod.setTarifa(newTarifa);

    this.parkingMeterRepository.save(parkingMeterMod);

    var updatedParkingMeter =
        parkingMeterRepository.findById(parkingMeterMod.getId()).orElseThrow();

    Assertions.assertThat(updatedParkingMeter).isNotNull();
    Assertions.assertThat(updatedParkingMeter.getVersion()).isGreaterThanOrEqualTo(1L);
    Assertions.assertThat(updatedParkingMeter.getTarifa()).isEqualTo(newTarifa);
  }

  @Test
  @Order(5)
  void deleteById() {
    this.parkingMeterRepository.deleteById(ID);
    var deleted = this.parkingMeterRepository.findById(ID).orElse(null);

    assertThat(deleted).isNull();
  }
}
