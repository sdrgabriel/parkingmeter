package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleSpentDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TicketService {
  Page<TicketDTO> findAll(Pageable pageable);

  TicketDTO getById(String id);

  TicketDTO create(TicketForm ticketForm);

  TicketDTO updatePayment(String id) throws TicketException;

  TicketDTO cancelTicket(String id) throws TicketException;

  void deleteById(String id);

  VehicleSpentDTO getTotalSpentByVehicle(String licensePlate) throws VehicleException;

  Page<TicketDTO> findTicketsByDateRange(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  Page<TicketDTO> findTicketsByStatus(PaymentStatusEnum status, Pageable pageable);

  Slice<BusyHoursDTO> findBusiestHour(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
