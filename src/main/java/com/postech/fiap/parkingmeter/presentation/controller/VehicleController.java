package com.postech.fiap.parkingmeter.presentation.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.VehicleForm;
import com.postech.fiap.parkingmeter.domain.service.VehicleService;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicle")
@RequiredArgsConstructor
public class VehicleController {

  private final VehicleService vehicleService;

  @GetMapping
  public ResponseEntity<Page<VehicleDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    var parkingMeters = vehicleService.findAll(pageable);
    return ResponseEntity.ok(parkingMeters);
  }

  @GetMapping("/{id}")
  public ResponseEntity<VehicleDTO> getById(@PathVariable String id) throws VehicleException {
    return ResponseEntity.ok(vehicleService.getById(id));
  }

  @PostMapping
  public ResponseEntity<VehicleDTO> create(@Valid @RequestBody VehicleForm vehicleForm) {
    var created = vehicleService.create(vehicleForm);
    return ResponseEntity.ok(created);
  }

  @PutMapping("/{id}")
  public ResponseEntity<VehicleDTO> updateById(
      @PathVariable String id, @Valid @RequestBody VehicleForm vehicleForm)
      throws VehicleException {
    return ResponseEntity.ok(vehicleService.updateById(id, vehicleForm));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    vehicleService.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
