package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.ParkingMeterForm;
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

  Slice<ParkingMeterCollectionDTO> getHighestEarningParkingMeter(
      String startDate, String endDate, Pageable pageable);

  ParkingSpaceDTO getAvailableSpace(String id, LocalDate date);

  TimesParkedDTO getTimesParkedWithDateRange(
      String parkingMeterId, String licensePlate, LocalDate begin, LocalDate end);

  Page<ParkingMeterDTO> findAllByCityOrNeighborhood(
      String city, String neighborhood, Pageable pageable);

  AmountEarnedDTO getParkingMeterEarningsWithDateRange(
      String parkingMeterId, LocalDate begin, LocalDate end);

  Page<AmountEarnedByLocalityDTO> getParkingMeterEarningsWithDateRangeByLocality(
      String city, String neighborhood, LocalDate begin, LocalDate end, Pageable pageable);
}
