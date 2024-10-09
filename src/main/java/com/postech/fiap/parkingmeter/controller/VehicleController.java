package com.postech.fiap.parkingmeter.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.VehicleForm;
import com.postech.fiap.parkingmeter.domain.service.VehicleService;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VehicleController {

  private final VehicleService vehicleService;

  @Operation(
      summary = "Find all vehicles",
      description = "Retrieves a paginated list of all vehicles.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<VehicleDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(vehicleService.findAll(pageable));
  }

  @Operation(summary = "Get vehicle by ID", description = "Retrieves a vehicle by its ID.")
  @Parameter(name = "id", description = "ID of the vehicle to be retrieved", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Vehicle retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<VehicleDTO> getById(@PathVariable String id) throws VehicleException {
    return ResponseEntity.ok(vehicleService.getById(id));
  }

  @Operation(summary = "Create a new vehicle", description = "Creates a new vehicle.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Vehicle created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<VehicleDTO> create(@Valid @RequestBody VehicleForm vehicleForm)
      throws VehicleException {
    var created = vehicleService.create(vehicleForm);
    return ResponseEntity.ok(created);
  }

  @Operation(summary = "Update vehicle by ID", description = "Updates a vehicle by its ID.")
  @Parameter(name = "id", description = "ID of the vehicle to be updated", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<VehicleDTO> updateById(
      @PathVariable String id, @Valid @RequestBody VehicleForm vehicleForm)
      throws VehicleException {
    return ResponseEntity.ok(vehicleService.updateById(id, vehicleForm));
  }

  @Operation(summary = "Delete vehicle by ID", description = "Deletes a vehicle by its ID.")
  @Parameter(name = "id", description = "ID of the vehicle to be deleted", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    vehicleService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
