package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.*;
import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import com.postech.fiap.parkingmeter.domain.repository.TicketRepository;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import com.postech.fiap.parkingmeter.domain.service.TicketService;
import com.postech.fiap.parkingmeter.domain.service.VehicleService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Transactional
public class TicketServiceImpl implements TicketService {

  private final ParkingMeterService parkingMeterService;
  private final TicketRepository ticketRepository;
  private final VehicleService vehicleService;
  private final ConverterToDTO converterToDTO;

  @Override
  @Transactional(readOnly = true)
  public Page<TicketDTO> findAll(Pageable pageable) {
    log.info("Find all tickets");
    return this.ticketRepository.findAll(pageable).map(this.converterToDTO::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public TicketDTO getById(String id) {
    log.info("Find one ticket");
    var ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(
                () -> new TicketException("Ticket code does not exist", HttpStatus.NOT_FOUND));
    return converterToDTO.toDto(ticket);
  }

  @Override
  public TicketDTO create(TicketForm ticketForm) {
    try {
      VehicleDTO vehicleDTO = this.vehicleService.getById(ticketForm.vehicleId());

      Vehicle vehicle = buildVehicle(vehicleDTO);

      ParkingMeterDTO parkingMeterDTO =
          this.parkingMeterService.getById(ticketForm.parkingMeterId());

      ParkingMeter parkingMeter = buildParkingMeter(parkingMeterDTO);

      var operatingHours = parkingMeter.getOperatingHours();
      LocalTime lt = LocalTime.parse(operatingHours.getEnd());
      LocalTime ltNow = LocalTime.now();

      if (ltNow.isAfter(lt)) {
        throw new TicketException("Parking meter closed, opening hours from %s to %s".formatted(operatingHours.getStart(), operatingHours.getEnd()), HttpStatus.BAD_REQUEST);
      }

      Optional<Ticket> pendingTicketByVehicleId =
          this.ticketRepository.findPendingTicketByVehicleId(ticketForm.vehicleId());

      if (pendingTicketByVehicleId != null && pendingTicketByVehicleId.isPresent()) {
        throw new TicketException(
            "The vehicle is already parked at a parking meter", HttpStatus.BAD_REQUEST);
      }

      List<Ticket> pendingTicketsByParkingMeterId =
          this.ticketRepository.findPendingTicketsByParkingMeterId(ticketForm.parkingMeterId());

      long pendingTicketCount = pendingTicketsByParkingMeterId.size();

      if (pendingTicketCount >= parkingMeter.getAvailableSpaces()) {
        throw new TicketException(
            "This parking meter has no available spaces", HttpStatus.BAD_REQUEST);
      }

      return converterToDTO.toDto(ticketRepository.save(buildTicket(vehicle, parkingMeter)));
    } catch (TicketException | ParkingMeterException | VehicleException e) {
      throw new TicketException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public TicketDTO updatePayment(String id) throws TicketException {
    Ticket ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketException("Ticket not found", HttpStatus.NOT_FOUND));

    var hasCanceled = ticket.getPaymentStatus() == PaymentStatusEnum.CANCELLED;
    var hasCharged = ticket.getPaymentStatus() == PaymentStatusEnum.PAID;
    if (hasCanceled || hasCharged) {
      throw new TicketException("Unable to update this ticket", HttpStatus.BAD_REQUEST);
    }

    var hourNow = LocalDateTime.now();
    var totalAmountCharged =
        getTotalAmountCharged(
            ticket.getStartTime(),
            hourNow,
            ticket.getParkingMeter().getRate().getFirstHour(),
            ticket.getParkingMeter().getRate().getAdditionalHours());

    ticket.setTotalAmountCharged(totalAmountCharged);
    ticket.setEndTime(hourNow);
    ticket.setPaymentStatus(PaymentStatusEnum.PAID);

    Ticket updatedTicket = this.ticketRepository.save(ticket);

    return converterToDTO.toDto(updatedTicket);
  }

  @Override
  public TicketDTO cancelTicket(String id) throws TicketException {
    Ticket ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketException("Ticket not found", HttpStatus.NOT_FOUND));

    if (ticket.getPaymentStatus() == PaymentStatusEnum.CANCELLED) {
      throw new TicketException("The ticket has already been canceled", HttpStatus.BAD_REQUEST);
    }

    LocalDateTime hourNow = LocalDateTime.now();

    long minutesDiff = ChronoUnit.MINUTES.between(ticket.getStartTime(), hourNow);

    final int TIME_LIMIT_MINUTES = 5;
    if (minutesDiff >= TIME_LIMIT_MINUTES) {
      throw new TicketException(
          "Ticket cannot be cancelled, grace period reached", HttpStatus.BAD_REQUEST);
    }

    ticket.setPaymentStatus(PaymentStatusEnum.CANCELLED);

    Ticket updatedTicket = this.ticketRepository.save(ticket);

    return converterToDTO.toDto(updatedTicket);
  }

  @Override
  public void deleteById(String id) {
    log.info("Delete ticket by id: {}", id);
    this.ticketRepository.deleteById(id);
  }

  @Override
  @Cacheable(value = "totalGastoVeiculo", key = "#licensePlate")
  @Transactional(readOnly = true)
  public VehicleSpentDTO getTotalSpentByVehicle(String licensePlate) throws VehicleException {
    List<Ticket> tickets = ticketRepository.findByVehicleLicensePlate(licensePlate);

    if (tickets.isEmpty()) {
      throw new VehicleException(
          "No ticket found for vehicle with license plate: " + licensePlate,
          HttpStatus.NOT_FOUND);
    }

    return VehicleSpentDTO.builder()
        .licensePlate(licensePlate)
        .totalSpent(tickets.stream().mapToDouble(Ticket::getTotalAmountCharged).sum())
        .build();
  }

  @Override
  @Cacheable(
      value = "ticketsPorIntervaloDeData",
      key = "#startDate + '-' + #endDate + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
  @Transactional(readOnly = true)
  public Page<TicketDTO> findTicketsByDateRange(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

    if (startDate.isAfter(endDate)) {
      throw new TicketException("Start date is greater than end date", HttpStatus.BAD_REQUEST);
    }
    return ticketRepository
        .findByStartTimeBetween(startDate, endDate, pageable)
        .map(converterToDTO::toDto);
  }

  @Override
  @Cacheable(
      value = "ticketsPorStatus",
      key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
  @Transactional(readOnly = true)
  public Page<TicketDTO> findTicketsByStatus(PaymentStatusEnum status, Pageable pageable) {
    return ticketRepository.findByPaymentStatus(status, pageable).map(converterToDTO::toDto);
  }

  @Override
  @Cacheable(value = "busyHours", key = "#startDate + '_' + #endDate")
  @Transactional(readOnly = true)
  public Slice<BusyHoursDTO> findBusiestHour(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return ticketRepository.findBusiestHour(startDate, endDate, pageable);
  }

  private double getTotalAmountCharged(
      LocalDateTime startHour,
      LocalDateTime endHour,
      double valueFirstHour,
      double valueOtherHours) {
    long totalHours = ChronoUnit.MINUTES.between(startHour, endHour);
    double roundedHours = Math.ceil(totalHours / 60.0);

    if (roundedHours <= 1) {
      return valueFirstHour;
    }

    double otherHoursCharge = (roundedHours - 1) * valueOtherHours;

    return valueFirstHour + otherHoursCharge;
  }

  private Vehicle buildVehicle(VehicleDTO vehicleDTO) {
    Address address =
        Address.builder()
            .city(vehicleDTO.getOwner().getAddress().getCity())
            .street(vehicleDTO.getOwner().getAddress().getStreet())
            .neighborhood(vehicleDTO.getOwner().getAddress().getNeighborhood())
            .state(vehicleDTO.getOwner().getAddress().getState())
            .zipCode(vehicleDTO.getOwner().getAddress().getZipCode())
            .number(vehicleDTO.getOwner().getAddress().getNumber())
            .complement(vehicleDTO.getOwner().getAddress().getComplement())
            .build();

    Owner owner =
        Owner.builder()
                .id(vehicleDTO.getOwner().getId())
            .cpf(vehicleDTO.getOwner().getCpf())
            .address(address)
            .email(vehicleDTO.getOwner().getEmail())
            .name(vehicleDTO.getOwner().getName())
            .phone(vehicleDTO.getOwner().getPhone())
            .build();

    return Vehicle.builder()
        .id(vehicleDTO.getId())
        .color(vehicleDTO.getColor())
        .licensePlate(vehicleDTO.getLicensePlate())
        .model(vehicleDTO.getModel())
        .owner(owner)
        .build();
  }

  private ParkingMeter buildParkingMeter(ParkingMeterDTO parkingMeterDTO) {
    return ParkingMeter.builder()
        .id(parkingMeterDTO.getId())
        .operatingHours(
            OperationHours.builder()
                .end(parkingMeterDTO.getOperatingHours().getEnd())
                .start(parkingMeterDTO.getOperatingHours().getStart())
                .build())
        .rate(
            Rate.builder()
                .additionalHours(parkingMeterDTO.getRate().getAdditionalHours())
                .firstHour(parkingMeterDTO.getRate().getFirstHour())
                .build())
        .availableSpaces(parkingMeterDTO.getAvailableSpaces())
        .address(
            Address.builder()
                .state(parkingMeterDTO.getAddress().getState())
                .street(parkingMeterDTO.getAddress().getStreet())
                .neighborhood(parkingMeterDTO.getAddress().getNeighborhood())
                .city(parkingMeterDTO.getAddress().getCity())
                .zipCode(parkingMeterDTO.getAddress().getZipCode())
                .number(parkingMeterDTO.getAddress().getNumber())
                .complement(parkingMeterDTO.getAddress().getComplement())
                .build())
        .version(parkingMeterDTO.getVersion())
        .build();
  }

  private Ticket buildTicket(Vehicle vehicle, ParkingMeter parkingMeter) {
    return Ticket.builder()
        .totalAmountCharged(0.00)
        .startTime(LocalDateTime.now())
        .endTime(null)
        .paymentStatus(PaymentStatusEnum.PENDING)
        .parkingMeter(parkingMeter)
        .vehicle(vehicle)
        .build();
  }
}
