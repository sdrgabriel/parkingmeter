package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.repository.OwnerRepository;
import com.postech.fiap.parkingmeter.domain.service.OwnerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    @Autowired
    private OwnerRepository ownerRepository;

    @Override
    public List<OwnerDTO> findAll() {

        return this.ownerRepository
                .findAll()
                .stream()
                .map(OwnerDTO::toDTO)
                .toList();
    }

    @Override
    public OwnerDTO getById(String id) {
        return OwnerDTO.toDTO(this.ownerRepository.findById(id)
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
}
