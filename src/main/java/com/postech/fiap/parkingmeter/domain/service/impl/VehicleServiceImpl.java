package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.Vehicle;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.VehicleForm;
import com.postech.fiap.parkingmeter.domain.repository.OwnerRepository;
import com.postech.fiap.parkingmeter.domain.repository.VehicleRepository;
import com.postech.fiap.parkingmeter.domain.service.VehicleService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
@Transactional
public class VehicleServiceImpl implements VehicleService {

  private final VehicleRepository vehicleRepository;
  private final ConverterToDTO converterToDTO;
  private final OwnerRepository ownerRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<VehicleDTO> findAll(Pageable pageable) {
    log.info("Find all vehicles");
    return vehicleRepository.findAll(pageable).map(converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public VehicleDTO getById(String id) throws VehicleException {
    log.info("Find vehicle by id");
    return vehicleRepository
        .findById(id)
        .map(converterToDTO::toDto)
        .orElseThrow(() -> new VehicleException("Not found Vehicle", HttpStatus.NOT_FOUND));
  }

  @Override
  public VehicleDTO create(VehicleForm vehicleForm) throws VehicleException {
    log.info("Create vehicle");
    Owner owner =
        ownerRepository
            .findById(vehicleForm.owner_id())
            .orElseThrow(() -> new VehicleException("Not found Vehicle", HttpStatus.NOT_FOUND));

    Vehicle vehicle =
        Vehicle.builder()
            .color(vehicleForm.color())
            .licensePlate(vehicleForm.licensePlate())
            .model(vehicleForm.model())
            .owner(owner)
            .build();
    return converterToDTO.toDto(vehicleRepository.save(vehicle));
  }

  @Override
  public VehicleDTO updateById(String id, VehicleForm vehicleForm) throws VehicleException {
    log.info("Update vehicle");
    Vehicle vehicle =
        vehicleRepository
            .findById(id)
            .orElseThrow(() -> new VehicleException("Not found Vehicle", HttpStatus.NOT_FOUND));

    vehicle.setColor(vehicleForm.color());
    vehicle.setLicensePlate(vehicleForm.licensePlate());
    vehicle.setModel(vehicleForm.model());

    return converterToDTO.toDto(vehicleRepository.save(vehicle));
  }

  @Override
  public void deleteById(String id) {
    log.info("Delete vehicle");
    vehicleRepository.deleteById(id);
  }
}
