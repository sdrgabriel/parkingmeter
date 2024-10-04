package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.model.parkingmeter.Endereco;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ParkingMeterService {

  Page<ParkingMeterDTO> findAll(Pageable pageable);

  ParkingMeterDTO getById(String id);

  ParkingMeterDTO create(ParkingMeterForm parkingMeterForm) throws ParkingMeterException;

  ParkingMeterDTO updateById(String id, ParkingMeterForm parkingMeterForm);

  void deleteById(String id);

  Endereco getEnderecoByCep(String cep);

  Slice<ParkingMeterArrecadacaoDTO> getParquimetroMaisArrecadado(
      String dataInicio, String dataFim, Pageable pageable);

  ParkingSpaceDTO getAvailableSpace(String id, LocalDate date);

  TimesParkedDTO getTimesParked(String parkingMeterId, String licensePlate);

  TimesParkedDTO getTimesParkedWithDateRange(
      String parkingMeterId, String licensePlate, LocalDate begin, LocalDate end);

  Page<ParkingMeterDTO> findAllByCidadeOrBairro(String cidade, String bairro, Pageable pageable);

  AmountEarnedDTO getParkingMeterEarnedWithDataRange(
      String parkingMeterId, LocalDate begin, LocalDate end);

  Page<AmountEarnedByLocalityDTO> getParkingMeterEarnedWithDataRangeByLocality(
      String cidade, String bairro, LocalDate begin, LocalDate end, Pageable pageable);
}
