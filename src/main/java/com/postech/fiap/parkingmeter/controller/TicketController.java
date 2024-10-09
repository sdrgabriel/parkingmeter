package com.postech.fiap.parkingmeter.controller;

import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleSpentDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import com.postech.fiap.parkingmeter.domain.service.TicketService;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
public class TicketController {

  private final TicketService ticketService;

  @Operation(
      summary = "Find all tickets",
      description = "Retrieves a paginated list of all tickets.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping
  public ResponseEntity<Page<TicketDTO>> findAll(@PageableDefault(size = 15) Pageable pageable) {
    return ResponseEntity.ok(ticketService.findAll(pageable));
  }

  @Operation(summary = "Get ticket by ID", description = "Retrieves a ticket by its ID.")
  @Parameter(name = "id", description = "ID of the ticket to be retrieved", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/{id}")
  public ResponseEntity<TicketDTO> getById(@PathVariable String id) {
    return ResponseEntity.ok(ticketService.getById(id));
  }

  @Operation(summary = "Create a new ticket", description = "Creates a new ticket.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PostMapping
  public ResponseEntity<TicketDTO> create(@Valid @RequestBody TicketForm ticketForm) {
    return ResponseEntity.ok(ticketService.create(ticketForm));
  }

  @Operation(summary = "Delete ticket by ID", description = "Deletes a ticket by its ID.")
  @Parameter(name = "id", description = "ID of the ticket to be deleted", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Ticket deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteById(@PathVariable String id) {
    ticketService.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(
      summary = "Update payment status of a ticket",
      description = "Updates the payment status of a ticket by its ID.")
  @Parameter(
      name = "id",
      description = "ID of the ticket to update payment status",
      required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PatchMapping("/{id}/payment")
  public ResponseEntity<TicketDTO> updatePayment(@PathVariable String id) throws TicketException {
    return ResponseEntity.ok(ticketService.updatePayment(id));
  }

  @Operation(summary = "Cancel a ticket", description = "Cancels a ticket by its ID.")
  @Parameter(name = "id", description = "ID of the ticket to cancel", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Ticket canceled successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @PatchMapping("/{id}/cancel")
  public ResponseEntity<TicketDTO> cancelTicket(@PathVariable String id) throws TicketException {
    return ResponseEntity.ok(ticketService.cancelTicket(id));
  }

  @Operation(
      summary = "Get total spent by vehicle",
      description =
          "Retrieves the total amount spent by a vehicle identified by its license plate.")
  @Parameter(name = "licensePlate", description = "License plate of the vehicle", required = true)
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Total spent by vehicle retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/total-spent/{licensePlate}")
  public ResponseEntity<VehicleSpentDTO> getTotalSpentByVehicle(@PathVariable String licensePlate)
      throws VehicleException {
    return ResponseEntity.ok(ticketService.getTotalSpentByVehicle(licensePlate));
  }

  @Operation(
      summary = "Search tickets by date range",
      description = "Retrieves a paginated list of tickets within the specified date range.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/search-tickets")
  public ResponseEntity<Page<TicketDTO>> searchTicketsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      Pageable pageable) {
    return ResponseEntity.ok(ticketService.findTicketsByDateRange(startDate, endDate, pageable));
  }

  @Operation(
      summary = "Search tickets by status",
      description = "Retrieves a paginated list of tickets based on their payment status.")
  @Parameter(
      name = "status",
      description = "Payment status of the tickets to search",
      required = true)
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/status")
  public ResponseEntity<Page<TicketDTO>> searchTicketsByStatus(
      @RequestParam PaymentStatusEnum status, Pageable pageable) {
    Page<TicketDTO> tickets = ticketService.findTicketsByStatus(status, pageable);
    return ResponseEntity.ok(tickets);
  }

  @Operation(
      summary = "Get busiest hour",
      description = "Retrieves the busiest hour within the specified date range.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Busiest hour retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  @GetMapping("/busiest-hour")
  public ResponseEntity<Slice<BusyHoursDTO>> getBusiestHour(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
      Pageable pageable) {
    return ResponseEntity.ok(ticketService.findBusiestHour(startDate, endDate, pageable));
  }
}
