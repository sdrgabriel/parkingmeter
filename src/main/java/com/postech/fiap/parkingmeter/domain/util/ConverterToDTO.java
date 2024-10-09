package com.postech.fiap.parkingmeter.domain.util;

import com.postech.fiap.parkingmeter.domain.model.*;
import com.postech.fiap.parkingmeter.domain.model.dto.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConverterToDTO {

  private final ModelMapper modelMapper;

  public OwnerDTO toDto(final Owner model) {
    return modelMapper.map(model, OwnerDTO.class);
  }

  public ParkingMeterDTO toDto(final ParkingMeter model) {
    return modelMapper.map(model, ParkingMeterDTO.class);
  }

  public TicketDTO toDto(final Ticket model) {
    return modelMapper.map(model, TicketDTO.class);
  }

  public VehicleDTO toDto(final Vehicle model) {
    return modelMapper.map(model, VehicleDTO.class);
  }

  public AddressDTO toDto(final Address model) {
    return modelMapper.map(model, AddressDTO.class);
  }
}
