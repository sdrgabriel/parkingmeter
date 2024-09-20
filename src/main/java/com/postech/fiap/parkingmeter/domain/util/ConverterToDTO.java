package com.postech.fiap.parkingmeter.domain.util;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.Vehicle;
import com.postech.fiap.parkingmeter.domain.model.dto.OwnerDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
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
}
