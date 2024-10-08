package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TotalVehicleOwnerDTO;
import java.util.List;

public interface OwnerService {

  List<OwnerDTO> findAll();

  OwnerDTO getById(String id);

  OwnerDTO create(OwnerDTO owner);

  OwnerDTO updateById(String id, OwnerDTO owner);

  void deleteById(String id);

  TotalVehicleOwnerDTO getVehicleCountByCpf(String id);
}
