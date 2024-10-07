package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TotalVehicleOwnerDTO;
import com.postech.fiap.parkingmeter.domain.repository.OwnerRepository;
import com.postech.fiap.parkingmeter.domain.repository.VehicleRepository;
import com.postech.fiap.parkingmeter.domain.service.OwnerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerServiceImpl implements OwnerService {

  private final OwnerRepository ownerRepository;
  private final VehicleRepository vehicleRepository;

  @Override
  @Transactional(readOnly = true)
  public List<OwnerDTO> findAll() {
    return this.ownerRepository.findAll().stream().map(OwnerDTO::toDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public OwnerDTO getById(String id) {
    return OwnerDTO.toDTO(
        this.ownerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Proprietario não encontrado")));
  }

  @Override
  public OwnerDTO create(OwnerDTO owner) {
    return OwnerDTO.toDTO(this.ownerRepository.save(Owner.toEntity(owner)));
  }

  @Override
  public OwnerDTO updateById(String id, OwnerDTO ownerDTO) {
    var ownerEntity = Owner.toEntity(ownerDTO);
    ownerEntity.setId(id);

    return OwnerDTO.toDTO(this.ownerRepository.save(ownerEntity));
  }

  @Override
  public void deleteById(String id) {
    this.ownerRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public TotalVehicleOwnerDTO getVehicleCountByCpf(String id) {
    OwnerDTO owner = getById(id);
    long total = vehicleRepository.countByProprietarioCpf(id);
    return TotalVehicleOwnerDTO.builder().owner(owner).totalVeiculo(total).build();
  }
}
