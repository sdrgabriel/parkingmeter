package com.postech.fiap.parkingmeter.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.parkingmeter.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parking-meter")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ParkingMeterController {

  private final ParkingMeterService parkingMeterService;

  @Operation(summary = "Retrieve all parking meters")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of parking meters retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<ParkingMeterDTO>> findAll(
      @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(parkingMeterService.findAll(pageable));
  }

  @Operation(summary = "Retrieve a parking meter by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Parking meter retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<ParkingMeterDTO> getById(@PathVariable String id) {
    return ResponseEntity.ok(parkingMeterService.getById(id));
  }

  @Operation(summary = "Create a new parking meter")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Parking meter created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<ParkingMeterDTO> create(
      @Valid @RequestBody ParkingMeterForm parkingMeterForm) {
    return ResponseEntity.ok(parkingMeterService.create(parkingMeterForm));
  }

  @Operation(summary = "Update a parking meter by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Parking meter updated successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<ParkingMeterDTO> updateById(
      @PathVariable String id, @Valid @RequestBody ParkingMeterForm parkingMeterForm) {
    return ResponseEntity.ok(parkingMeterService.updateById(id, parkingMeterForm));
  }

  @Operation(summary = "Delete a parking meter by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Parking meter deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    parkingMeterService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Rank parking meters by earnings within a date range")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ranking of parking meters by earnings retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/rank-earnings-by-date")
  public ResponseEntity<Slice<ParkingMeterCollectionDTO>> rankParkingMetersByEarningsByDate(
      @RequestParam String startDate, @RequestParam String endDate, Pageable pageable) {
    return ResponseEntity.ok(
        parkingMeterService.getHighestEarningParkingMeter(startDate, endDate, pageable));
  }

  @Operation(summary = "Get available parking space for a specific date")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Available parking space retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/available")
  public ResponseEntity<ParkingSpaceDTO> getAvailableSpace(
      @RequestParam("id") @NotEmpty(message = "The id field cannot be empty or null") String id,
      @RequestParam("date") @NotNull(message = "The date field cannot be null") LocalDate date) {
    return ResponseEntity.ok(parkingMeterService.getAvailableSpace(id, date));
  }

  @Operation(summary = "Get times parked within a date range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Times parked retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/times-parked-date-range")
  public ResponseEntity<TimesParkedDTO> getTimesParkedWithDateRange(
      @RequestParam("parkingMeterId")
          @NotEmpty(message = "The parkingMeterId field cannot be empty or null")
          String parkingMeterId,
      @RequestParam("licensePlate")
          @NotEmpty(message = "The licensePlate field cannot be empty or null")
          String licensePlate,
      @RequestParam("begin") @NotNull(message = "The begin field cannot be null") LocalDate begin,
      @RequestParam(name = "end", required = false) LocalDate end) {
    return ResponseEntity.ok(
        parkingMeterService.getTimesParkedWithDateRange(parkingMeterId, licensePlate, begin, end));
  }

  @Operation(summary = "Find parking meters by city or neighborhood")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Parking meters retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/find-locality")
  public ResponseEntity<Page<ParkingMeterDTO>> findAllByCityOrNeighborhood(
      @RequestParam(name = "city", required = false) String city,
      @RequestParam(name = "neighborhood", required = false) String neighborhood,
      @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(
        parkingMeterService.findAllByCityOrNeighborhood(city, neighborhood, pageable));
  }

  @Operation(summary = "Get parking meter earnings within a date range")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parking meter earnings retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Parking meter not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/earned")
  public ResponseEntity<AmountEarnedDTO> getParkingMeterEarningsWithDateRange(
      @RequestParam("parkingMeterId")
          @NotEmpty(message = "The parkingMeterId field cannot be empty or null")
          String parkingMeterId,
      @RequestParam("begin") @NotNull(message = "The begin field cannot be null") LocalDate begin,
      @RequestParam(name = "end", required = false) LocalDate end) {
    return ResponseEntity.ok(
        parkingMeterService.getParkingMeterEarningsWithDateRange(parkingMeterId, begin, end));
  }

  @Operation(summary = "Get parking meter earnings by locality within a date range")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Parking meter earnings by locality retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/earned-by-locality")
  public ResponseEntity<Page<AmountEarnedByLocalityDTO>>
      getParkingMeterEarnedWithDataRangeByLocality(
          @RequestParam(name = "city", required = false) String city,
          @RequestParam(name = "neighborhood", required = false) String neighborhood,
          @RequestParam("begin") @NotNull(message = "The begin field cannot be null")
              LocalDate begin,
          @RequestParam(name = "end", required = false) LocalDate end,
          @PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(
        parkingMeterService.getParkingMeterEarningsWithDateRangeByLocality(
            city, neighborhood, begin, end, pageable));
  }
}
