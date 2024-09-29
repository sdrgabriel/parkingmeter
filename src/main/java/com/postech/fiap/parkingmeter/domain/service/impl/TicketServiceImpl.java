package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.Owner;
import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.Vehicle;
import com.postech.fiap.parkingmeter.domain.model.dto.ParkingMeterDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.TicketDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.VehicleDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.repository.TicketRepository;
import com.postech.fiap.parkingmeter.domain.service.ParkingMeterService;
import com.postech.fiap.parkingmeter.domain.service.TicketService;
import com.postech.fiap.parkingmeter.domain.service.VehicleService;
import com.postech.fiap.parkingmeter.domain.util.ConverterToDTO;
import com.postech.fiap.parkingmeter.infrastructure.exception.ParkingMeterException;
import com.postech.fiap.parkingmeter.infrastructure.exception.TicketException;
import com.postech.fiap.parkingmeter.infrastructure.exception.VehicleException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
@Slf4j
@ImportAutoConfiguration(TransactionAutoConfiguration.class)
public class TicketServiceImpl implements TicketService {
  @Autowired private final TicketRepository ticketRepository;

  private final VehicleService vehicleService;
  private final ParkingMeterService parkingMeterService;

  private final ConverterToDTO converterToDTO;

  @Override
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
                () ->
                    new ParkingMeterException("Ticket code does not exist", HttpStatus.NOT_FOUND));
    return converterToDTO.toDto(ticket);
  }

  @Override
  public TicketDTO create(TicketForm ticketForm)
      throws TicketException, ParkingMeterException, VehicleException {
    try {
      VehicleDTO vehicleDTO = this.vehicleService.getById(ticketForm.vehicle_id());
      if (vehicleDTO == null) {
        throw new VehicleException("Veículo não encontrado", HttpStatus.NOT_FOUND);
      }
      Vehicle vehicle = buildVehicle(vehicleDTO);

      ParkingMeterDTO parkingMeterDTO =
          this.parkingMeterService.getById(ticketForm.parking_meter_id());
      if (parkingMeterDTO == null) {
        throw new ParkingMeterException("Parquimetro não encontrado", HttpStatus.NOT_FOUND);
      }
      ParkingMeter parkingMeter = buildParkingMeter(parkingMeterDTO);

      Ticket ticket = buildTicket(vehicle, parkingMeter);
      Ticket createdTicket = this.ticketRepository.save(ticket);

      return converterToDTO.toDto(createdTicket);
    } catch (Exception e) {
      throw new TicketException(
          "Não foi possível criar o ticket", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  public TicketDTO updatePayment(String id) throws TicketException {
    Ticket ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketException("Ticket não encontrado", HttpStatus.NOT_FOUND));

    var hasCanceled = ticket.getStatusPagamento() == Ticket.StatusPagamento.CANCELADO;
    var hasCharged = ticket.getStatusPagamento() == Ticket.StatusPagamento.PAGO;
    if (hasCanceled || hasCharged) {
      throw new TicketException("Não foi possivel atualizar este ticket", HttpStatus.BAD_REQUEST);
    }

    var hourNow = LocalDateTime.now();
    var totalAmountCharged =
        getTotalAmountCharged(
            ticket.getHorarioInicio(),
            hourNow,
            ticket.getParquimetro().getTarifa().primeiraHora(),
            ticket.getParquimetro().getTarifa().demaisHoras());

    ticket.setValorTotalCobrado(totalAmountCharged);
    ticket.setHorarioFim(hourNow);
    ticket.setStatusPagamento(Ticket.StatusPagamento.PAGO);

    Ticket updatedTicket = this.ticketRepository.save(ticket);

    return converterToDTO.toDto(updatedTicket);
  }

  public TicketDTO cancelTicket(String id) throws TicketException {
    Ticket ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketException("Ticket não encontrado", HttpStatus.NOT_FOUND));

    if (ticket.getStatusPagamento() == Ticket.StatusPagamento.CANCELADO) {
      throw new TicketException("O ticket já foi cancelado", HttpStatus.BAD_REQUEST);
    }

    ticket.setStatusPagamento(Ticket.StatusPagamento.CANCELADO);
    ticket.setHorarioFim(LocalDateTime.now());

    Ticket updatedTicket = this.ticketRepository.save(ticket);

    return converterToDTO.toDto(updatedTicket);
  }

  @Override
  public void deleteById(String id) {
    log.info("Delete ticket by id: {}", id);
    this.ticketRepository.deleteById(id);
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
    Owner owner = Owner.builder().build(); // TO-DO: Obter dono;
    return Vehicle.builder()
        .color(vehicleDTO.color())
        .licensePlate(vehicleDTO.licensePlate())
        .model(vehicleDTO.model())
        .owner(owner)
        .build();
  }

  private ParkingMeter buildParkingMeter(ParkingMeterDTO parkingMeterDTO) {
    return ParkingMeter.builder()
        .horarioFuncionamento(parkingMeterDTO.getHorarioFuncionamento())
        .tarifa(parkingMeterDTO.getTarifa())
        .vagasDisponiveis(parkingMeterDTO.getVagasDisponiveis())
        .endereco(parkingMeterDTO.getEndereco())
        .version(parkingMeterDTO.getVersion())
        .build();
  }

  private Ticket buildTicket(Vehicle vehicle, ParkingMeter parkingMeter) {
    return Ticket.builder()
        .valorTotalCobrado(0.00)
        .horarioInicio(LocalDateTime.now())
        .horarioFim(null)
        .statusPagamento(Ticket.StatusPagamento.PENDENTE)
        .parquimetro(parkingMeter)
        .veiculo(vehicle)
        .build();
  }
}
