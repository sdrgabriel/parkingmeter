package com.postech.fiap.parkingmeter.domain.service.impl;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.Vehicle;
import com.postech.fiap.parkingmeter.domain.model.dto.*;
import com.postech.fiap.parkingmeter.domain.model.dto.forms.TicketForm;
import com.postech.fiap.parkingmeter.domain.model.enums.StatusPagamentoEnum;
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
@Transactional
@RequiredArgsConstructor
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
      VehicleDTO vehicleDTO = this.vehicleService.getById(ticketForm.vehicle_id());

      Vehicle vehicle = buildVehicle(vehicleDTO);

      ParkingMeterDTO parkingMeterDTO =
          this.parkingMeterService.getById(ticketForm.parking_meter_id());

      ParkingMeter parkingMeter = buildParkingMeter(parkingMeterDTO);

      Optional<Ticket> pendingTicketByVehicleId =
          this.ticketRepository.findPendingTicketByVehicleId(ticketForm.vehicle_id());

      if (pendingTicketByVehicleId != null && pendingTicketByVehicleId.isPresent()) {
        throw new TicketException(
            "O veículo já está estacionado em um parquímetro", HttpStatus.BAD_REQUEST);
      }

      List<Ticket> pendingTicketsByParkingMeterId =
          this.ticketRepository.findPendingTicketsByParkingMeterId(ticketForm.parking_meter_id());

      long pendingTicketCount = pendingTicketsByParkingMeterId.size();

      if (pendingTicketCount >= parkingMeter.getVagasDisponiveis()) {
        throw new TicketException(
            "Este parquímetro não tem vagas disponíveis", HttpStatus.BAD_REQUEST);
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
            .orElseThrow(() -> new TicketException("Ticket não encontrado", HttpStatus.NOT_FOUND));

    var hasCanceled = ticket.getStatusPagamento() == StatusPagamentoEnum.CANCELADO;
    var hasCharged = ticket.getStatusPagamento() == StatusPagamentoEnum.PAGO;
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
    ticket.setStatusPagamento(StatusPagamentoEnum.PAGO);

    Ticket updatedTicket = this.ticketRepository.save(ticket);

    return converterToDTO.toDto(updatedTicket);
  }

  @Override
  public TicketDTO cancelTicket(String id) throws TicketException {
    Ticket ticket =
        this.ticketRepository
            .findById(id)
            .orElseThrow(() -> new TicketException("Ticket não encontrado", HttpStatus.NOT_FOUND));

    if (ticket.getStatusPagamento() == StatusPagamentoEnum.CANCELADO) {
      throw new TicketException("O ticket já foi cancelado", HttpStatus.BAD_REQUEST);
    }

    LocalDateTime hourNow = LocalDateTime.now();

    long minutesDiff = ChronoUnit.MINUTES.between(ticket.getHorarioInicio(), hourNow);

    final int TIME_LIMIT_MINUTES = 5;
    if (minutesDiff >= TIME_LIMIT_MINUTES) {
      throw new TicketException(
          "O ticket não pode ser cancelado, tempo de tolerância atingido", HttpStatus.BAD_REQUEST);
    }

    ticket.setStatusPagamento(StatusPagamentoEnum.CANCELADO);

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
  public VehicleSpentDTO obterTotalGastoPorVeiculo(String licensePlate) throws VehicleException {
    List<Ticket> tickets = ticketRepository.findByVeiculoLicensePlate(licensePlate);

    if (tickets.isEmpty()) {
      throw new VehicleException(
          "Nenhum ticket encontrado para o veículo com a placa: " + licensePlate,
          HttpStatus.NOT_FOUND);
    }

    return VehicleSpentDTO.builder()
        .licensePlate(licensePlate)
        .totalSpent(tickets.stream().mapToDouble(Ticket::getValorTotalCobrado).sum())
        .build();
  }

  @Override
  @Cacheable(
      value = "ticketsPorIntervaloDeData",
      key = "#startDate + '-' + #endDate + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
  @Transactional(readOnly = true)
  public Page<TicketDTO> buscarTicketsPorIntervaloDeData(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return ticketRepository
        .findByHorarioInicioBetween(startDate, endDate, pageable)
        .map(converterToDTO::toDto);
  }

  @Override
  @Cacheable(
      value = "ticketsPorStatus",
      key = "#status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
  @Transactional(readOnly = true)
  public Page<TicketDTO> buscarTicketsPorStatus(StatusPagamentoEnum status, Pageable pageable) {
    return ticketRepository.findByStatusPagamento(status, pageable).map(converterToDTO::toDto);
  }

  @Override
  @Cacheable(value = "busyHours", key = "#startDate + '_' + #endDate")
  @Transactional(readOnly = true)
  public Slice<BusyHoursDTO> buscarHorarioMaisMovimentado(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
    return ticketRepository.buscarHorarioMaisMovimentado(startDate, endDate, pageable);
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
    return Vehicle.builder()
        .id(vehicleDTO.getId())
        .color(vehicleDTO.getColor())
        .licensePlate(vehicleDTO.getLicensePlate())
        .model(vehicleDTO.getModel())
        .owner(vehicleDTO.getOwner())
        .build();
  }

  private ParkingMeter buildParkingMeter(ParkingMeterDTO parkingMeterDTO) {
    return ParkingMeter.builder()
        .id(parkingMeterDTO.getId())
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
        .horarioInicio(ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime())
        .horarioFim(null)
        .statusPagamento(StatusPagamentoEnum.PENDENTE)
        .parquimetro(parkingMeter)
        .veiculo(vehicle)
        .build();
  }
}
