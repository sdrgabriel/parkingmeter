package com.postech.fiap.parkingmeter.presentation.controller;

import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.RankedParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleSpentDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.service.TicketService;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

  private final TicketService ticketService;

  @GetMapping
  public ResponseEntity<Page<TicketDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    var tickets = this.ticketService.findAll(pageable);
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TicketDTO> getById(@PathVariable String id) {
    var get = this.ticketService.getById(id);
    return ResponseEntity.ok(get);
  }

  @PostMapping
  public ResponseEntity<TicketDTO> create(@Valid @RequestBody TicketForm ticketForm)
      throws VehicleException, TicketException {
    var created = this.ticketService.create(ticketForm);
    return ResponseEntity.ok(created);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    this.ticketService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{id}/payment")
  public ResponseEntity<TicketDTO> updatePayment(@PathVariable String id) throws TicketException {
    TicketDTO canceledTicket = this.ticketService.updatePayment(id);
    return ResponseEntity.ok(canceledTicket);
  }

  @PatchMapping("/{id}/cancel")
  public ResponseEntity<TicketDTO> cancelTicket(@PathVariable String id) throws TicketException {
    TicketDTO canceledTicket = this.ticketService.cancelTicket(id);
    return ResponseEntity.ok(canceledTicket);
  }

  @GetMapping("/total-gasto/{licensePlate}")
  public ResponseEntity<VehicleSpentDTO> obterTotalGastoPorVeiculo(
      @PathVariable String licensePlate) throws VehicleException {
    return ResponseEntity.ok(ticketService.obterTotalGastoPorVeiculo(licensePlate));
  }

  @GetMapping("/search/between-date")
  public ResponseEntity<Page<TicketDTO>> buscarTicketsPorIntervalo(
      @RequestParam LocalDateTime startDate,
      @RequestParam LocalDateTime endDate,
      Pageable pageable) {
    return ResponseEntity.ok(
        ticketService.buscarTicketsPorIntervaloDeData(startDate, endDate, pageable));
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<Page<TicketDTO>> buscarTicketsPorStatus(
      @PathVariable Ticket.StatusPagamento status, Pageable pageable) {
    Page<TicketDTO> tickets = ticketService.buscarTicketsPorStatus(status, pageable);
    return ResponseEntity.ok(tickets);
  }

  @GetMapping("/horario-mais-movimentado")
  public ResponseEntity<Page<BusyHoursDTO>> buscarHorarioMaisMovimentado(
      @RequestParam("startDate") String startDateStr,
      @RequestParam("endDate") String endDateStr,
      Pageable pageable) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate startDate = LocalDate.parse(startDateStr, formatter);
    LocalDate endDate = LocalDate.parse(endDateStr, formatter);

    return ResponseEntity.ok(ticketService.buscarHorarioMaisMovimentado(startDate, endDate, pageable));
  }
}
