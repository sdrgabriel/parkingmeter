package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.VehicleForm;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VehicleService {

  Page<VehicleDTO> findAll(Pageable pageable);

  VehicleDTO getById(String id) throws VehicleException;

  VehicleDTO create(VehicleForm vehicleForm);

  VehicleDTO updateById(String id, VehicleForm vehicleForm) throws VehicleException;

  void deleteById(String id);
}
