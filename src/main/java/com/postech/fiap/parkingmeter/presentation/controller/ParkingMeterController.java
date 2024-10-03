package com.postech.fiap.parkingmeter.presentation.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import jakarta.validation.Valid;
import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parkingmeter")
@RequiredArgsConstructor
public class ParkingMeterController {

  private final ParkingMeterService parkingMeterService;

  @GetMapping
  public ResponseEntity<Page<ParkingMeterDTO>> findAll(
      @PageableDefault(size = 15) Pageable pageable) {
    var parkingMeters = this.parkingMeterService.findAll(pageable);
    return ResponseEntity.ok(parkingMeters);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ParkingMeterDTO> getById(@PathVariable String id) {
    var get = this.parkingMeterService.getById(id);
    return ResponseEntity.ok(get);
  }

  @PostMapping
  public ResponseEntity<ParkingMeterDTO> create(
      @Valid @RequestBody ParkingMeterForm parkingMeterForm) {
    var created = this.parkingMeterService.create(parkingMeterForm);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ParkingMeterDTO> updateById(
      @PathVariable String id, @Valid @RequestBody ParkingMeterForm parkingMeterForm) {
    var updated = this.parkingMeterService.updateById(id, parkingMeterForm);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    this.parkingMeterService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/rank-arrecadacao-por-data")
  public ResponseEntity<Slice<RankedParkingMeterDTO>> rankParquimetrosPorArrecadacaoPorData(
      @RequestParam LocalDate startDate, @RequestParam LocalDate endDate, Pageable pageable) {
    return ResponseEntity.ok(
        parkingMeterService.rankParquimetrosPorArrecadacaoPorData(startDate, endDate, pageable));
  }

  @GetMapping("/rank-arrecadacao-por-dia")
  public ResponseEntity<Slice<RankedParkingMeterDTO>> rankParquimetrosPorArrecadacaoPorDia(
      @RequestParam LocalDate startDate, @RequestParam LocalDate endDate, Pageable pageable) {
    return ResponseEntity.ok(
        parkingMeterService.rankParquimetrosPorArrecadacaoPorDia(startDate, endDate, pageable));
  }

  @GetMapping("/available")
  public ResponseEntity<ParkingSpaceDTO> getAvailableSpace(
          @RequestParam("id") @NotEmpty(message = "The id field cannot be empty or null") String id,
          @RequestParam("date") @NotNull(message = "The date field cannot be null") LocalDate date) {
    return ResponseEntity.ok(this.parkingMeterService.getAvailableSpace(id, date));
  }

  @GetMapping("/times-parked")
  public ResponseEntity<TimesParkedDTO> getTimesParked(
          @RequestParam("parkingMeterId") @NotEmpty(message = "The parkingMeterId field cannot be empty or null") String parkingMeterId,
          @RequestParam("licensePlate") @NotEmpty(message = "The licensePlate field cannot be empty or null") String licensePlate) {
    return ResponseEntity.ok(this.parkingMeterService.getTimesParked(parkingMeterId, licensePlate));
  }

  @GetMapping("/times-parked-date-range")
  public ResponseEntity<TimesParkedDTO> getTimesParkedWithDateRange(
          @RequestParam("parkingMeterId") @NotEmpty(message = "The parkingMeterId field cannot be empty or null") String parkingMeterId,
          @RequestParam("licensePlate") @NotEmpty(message = "The licensePlate field cannot be empty or null") String licensePlate,
          @RequestParam("begin") @NotNull(message = "The begin field cannot be null") LocalDate begin,
          @RequestParam(name = "end", required = false) LocalDate end) {
    return ResponseEntity.ok(this.parkingMeterService.getTimesParkedWithDateRange(parkingMeterId, licensePlate, begin, end));
  }

  @GetMapping("/find-locality")
  public ResponseEntity<Page<ParkingMeterDTO>> findAllByCidadeOrBairro(
          @RequestParam(name = "cidade", required = false) String cidade,
          @RequestParam(name = "bairro", required = false) String bairro,
          @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(this.parkingMeterService.findAllByCidadeOrBairro(cidade, bairro, pageable));
  }

  @GetMapping("/earned")
  public ResponseEntity<AmountEarnedDTO> getParkingMeterEarnedWithDataRange(
          @RequestParam("parkingMeterId") @NotEmpty(message = "The parkingMeterId field cannot be empty or null") String parkingMeterId,
          @RequestParam("begin") @NotNull(message = "The begin field cannot be null") LocalDate begin,
          @RequestParam(name = "end", required = false) LocalDate end){
    return ResponseEntity.ok(this.parkingMeterService.getParkingMeterEarnedWithDataRange(parkingMeterId, begin, end));
  }

  @GetMapping("/earned-by-locality")
  public ResponseEntity<Page<AmountEarnedByLocalityDTO>> getParkingMeterEarnedWithDataRangeByLocality(
          @RequestParam(name = "cidade", required = false) String cidade,
          @RequestParam(name = "bairro", required = false) String bairro,
          @RequestParam("begin") @NotNull(message = "The begin field cannot be null") LocalDate begin,
          @RequestParam(name = "end", required = false) LocalDate end,
          @PageableDefault(size = 15) Pageable pageable){
    return ResponseEntity.ok(this.parkingMeterService.getParkingMeterEarnedWithDataRangeByLocality(cidade, bairro, begin, end, pageable));
  }
}
