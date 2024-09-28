package com.postech.fiap.parkingmeter.presentation.controller;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.OwnerForm;
import com.postech.fiap.parkingmeter.domain.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner")
public class OwnerController {

  @Autowired
  private OwnerService ownerService;

  @GetMapping("/hello")
  public ResponseEntity<String> getTest() {
    return ResponseEntity.ok("Hello World");
  }

  @GetMapping
  public ResponseEntity<List<OwnerDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    var owners = this.ownerService.findAll();
    return ResponseEntity.ok(owners);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OwnerDTO> getById(@PathVariable String id) {
    return ResponseEntity.ok(this.ownerService.getById(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<OwnerDTO> updateById(
      @PathVariable String id, @Valid @RequestBody OwnerDTO owner) {
    return ResponseEntity.ok(this.ownerService.updateById(id, owner));
  }

  @PostMapping
  public ResponseEntity<OwnerDTO> create(@Valid @RequestBody OwnerDTO owner) {
    return ResponseEntity.ok(this.ownerService.create(owner));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    this.ownerService.deleteById(id);
    return ResponseEntity.ok().build();
  }

}
