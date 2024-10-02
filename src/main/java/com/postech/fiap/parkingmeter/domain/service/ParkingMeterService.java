package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.RankedParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkingMeterService {

  Page<ParkingMeterDTO> findAll(Pageable pageable);

  ParkingMeterDTO getById(String id);

  ParkingMeterDTO create(ParkingMeterForm parkingMeterForm) throws ParkingMeterException;

  ParkingMeterDTO updateById(String id, ParkingMeterForm parkingMeterForm);

  void deleteById(String id);

  Endereco getEnderecoByCep(String cep);

  Page<RankedParkingMeterDTO> rankParquimetrosPorArrecadacaoPorData(
      LocalDate startDate, LocalDate endDate, Pageable pageable);

  Page<RankedParkingMeterDTO> rankParquimetrosPorArrecadacaoPorDia(
      LocalDate startDate, LocalDate endDate, Pageable pageable);
}
