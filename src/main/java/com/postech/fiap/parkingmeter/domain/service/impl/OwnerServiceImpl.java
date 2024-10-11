package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Address;
import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TotalVehicleOwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.OwnerForm;
import com.postech.fiap.parkingmeter.domain.repository.OwnerRepository;
import com.postech.fiap.parkingmeter.domain.repository.VehicleRepository;
import com.postech.fiap.parkingmeter.domain.service.OwnerService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.OwnerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Transactional
public class OwnerServiceImpl implements OwnerService {

  private final VehicleRepository vehicleRepository;
  private final OwnerRepository ownerRepository;
  private final ConverterToDTO converterToDTO;

  @Override
  @Transactional(readOnly = true)
  public Page<OwnerDTO> findAll(Pageable pageable) {
    return ownerRepository.findAll(pageable).map(converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public OwnerDTO getById(String id) {
    return ownerRepository
        .findById(id)
        .map(converterToDTO::toDto)
        .orElseThrow(() -> new OwnerException("Owner not found", HttpStatus.NOT_FOUND));
  }

  @Override
  public OwnerDTO create(OwnerForm ownerForm) {
    return converterToDTO.toDto(ownerRepository.save(buildOwner(ownerForm)));
  }

  @Override
  public OwnerDTO updateById(String id, OwnerForm ownerForm) {
    Owner owner =
        ownerRepository
            .findById(id)
            .orElseThrow(() -> new OwnerException("Owner not found", HttpStatus.NOT_FOUND));

    owner.setPhone(ownerForm.phone());
    owner.setName(ownerForm.name());
    owner.setCpf(ownerForm.cpf());
    owner.setEmail(ownerForm.email());
    owner.setAddress(buildAddress(ownerForm));

    return converterToDTO.toDto(ownerRepository.save(owner));
  }

  @Override
  public void deleteById(String id) {
    if (!ownerRepository.existsById(id)) {
      throw new OwnerException("Owner not found", HttpStatus.NOT_FOUND);
    }
    ownerRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public TotalVehicleOwnerDTO getVehicleCountByOwnerId(String id) {
    return TotalVehicleOwnerDTO.builder()
        .owner(getById(id))
        .totalVehicles(vehicleRepository.countByOwnerId(id))
        .build();
  }

  private Owner buildOwner(OwnerForm ownerForm) {
    return Owner.builder()
        .phone(ownerForm.phone())
        .name(ownerForm.name())
        .cpf(ownerForm.cpf())
        .email(ownerForm.email())
        .address(buildAddress(ownerForm))
        .build();
  }

  private Address buildAddress(OwnerForm ownerForm) {
    return Address.builder()
        .street(ownerForm.address().street())
        .neighborhood(ownerForm.address().neighborhood())
        .city(ownerForm.address().city())
        .state(ownerForm.address().state())
        .zipCode(ownerForm.address().zipCode())
        .number(ownerForm.address().number())
        .complement(ownerForm.address().complement())
        .build();
  }
}
