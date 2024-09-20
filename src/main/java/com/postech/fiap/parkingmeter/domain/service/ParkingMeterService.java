package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingMeterService {

  /*private final ParkingMeterRepository parkingMeterRepository;
  private final ConverterToDTO converterToDTO;

  @Transactional(readOnly = true)
  public Page<ParkingMeterDTO> findAll(Pageable pageable) {
    log.info("Find all parking meter");
    return parkingMeterRepository.findAll(pageable).map(converterToDTO::toDto);
  }*/
}
