package com.postech.fiap.parkingmeter.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TotalVehicleOwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.OwnerForm;
import com.postech.fiap.parkingmeter.domain.service.OwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OwnerController {

  private final OwnerService ownerService;

  @Operation(summary = "Retrieve all owners")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "List of owners retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<OwnerDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(ownerService.findAll(pageable));
  }

  @Operation(summary = "Retrieve an owner by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Owner retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<OwnerDTO> getById(@PathVariable String id) {
    return ResponseEntity.ok(ownerService.getById(id));
  }

  @Operation(summary = "Get the total number of vehicles owned by an owner")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total vehicle count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/vehicle-count")
  public ResponseEntity<TotalVehicleOwnerDTO> getVehicleCountById(@RequestParam String id) {
    TotalVehicleOwnerDTO totalVehicleOwnerDTO = ownerService.getVehicleCountByOwnerId(id);

    if (totalVehicleOwnerDTO == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(totalVehicleOwnerDTO);
  }

  @Operation(summary = "Update an owner by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Owner updated successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PutMapping("/{id}")
  public ResponseEntity<OwnerDTO> updateById(
      @PathVariable String id, @Valid @RequestBody OwnerForm ownerDTO) {
    return ResponseEntity.ok(ownerService.updateById(id, ownerDTO));
  }

  @Operation(summary = "Create a new owner")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Owner created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<OwnerDTO> create(@Valid @RequestBody OwnerForm ownerDTO) {
    return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.create(ownerDTO));
  }

  @Operation(summary = "Delete an owner by ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Owner deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Owner not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    ownerService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
