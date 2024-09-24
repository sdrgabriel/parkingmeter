package com.postech.fiap.parkingmeter.presentation.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.ParkingMeterForm;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parkingmeter")
@RequiredArgsConstructor
public class ParkingMeterController {

    private final ParkingMeterService parkingMeterService;

    @GetMapping
    public ResponseEntity<Page<ParkingMeterDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
        var parkingMeters = this.parkingMeterService.findAll(pageable);
        return ResponseEntity.ok(parkingMeters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingMeterDTO> getById(@PathVariable String id) {
        var get = this.parkingMeterService.getById(id);
        return ResponseEntity.ok(get);
    }

    @PostMapping
    public ResponseEntity<ParkingMeterDTO> create(@Valid @RequestBody ParkingMeterForm parkingMeterForm) {
        var created = this.parkingMeterService.create(parkingMeterForm);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingMeterDTO> updateById(@PathVariable String id, @Valid @RequestBody ParkingMeterForm parkingMeterForm) {
        var updated = this.parkingMeterService.updateById(id, parkingMeterForm);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable String id) {
        this.parkingMeterService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
