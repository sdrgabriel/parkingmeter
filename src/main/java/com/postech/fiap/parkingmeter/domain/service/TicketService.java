package com.postech.fiap.parkingmeter.domain.service;

import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;

import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TicketService {
  Page<TicketDTO> findAll(Pageable pageable);

  TicketDTO getById(String id);

  TicketDTO create(TicketForm ticketForm) throws TicketException, VehicleException;

  TicketDTO updatePayment(String id) throws TicketException;

  TicketDTO cancelTicket(String id)  throws TicketException;

  void deleteById(String id);
}
