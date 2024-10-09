package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TotalVehicleOwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.OwnerForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OwnerService {

  Page<OwnerDTO> findAll(Pageable pageable);

  OwnerDTO getById(String id);

  OwnerDTO create(OwnerForm owner);

  OwnerDTO updateById(String id, OwnerForm owner);

  void deleteById(String id);

  TotalVehicleOwnerDTO getVehicleCountByOwnerId(String id);
}
