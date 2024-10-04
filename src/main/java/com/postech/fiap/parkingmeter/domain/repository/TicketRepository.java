package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.postech.fiap.parkingmeter.domain.model.enums.StatusPagamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

  @Query("{ 'status_pagamento': 'PENDENTE', 'veiculo._id': ?0 }")
  Optional<Ticket> findPendingTicketByVehicleId(String vehicleId);

  @Query("{ 'status_pagamento': 'PENDENTE', 'parquimetro._id': ?0 }")
  List<Ticket> findPendingTicketsByParkingMeterId(String parkingMeterId);

  List<Ticket> findByVeiculoLicensePlate(String licensePlate);

  Page<Ticket> findByHorarioInicioBetween(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  Page<Ticket> findByStatusPagamento(StatusPagamentoEnum status, Pageable pageable);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'horario_inicio': { '$gte': ?0, '$lt': ?1 } } }",
        "{ '$group': { "
            + "'_id': { "
            + "'parquimetroId': '$parquimetro._id', "
            + "'horario': { '$hour': '$horario_inicio' } "
            + "}, "
            + "'totalTickets': { '$sum': 1 }, "
            + "'parquimetro': { '$first': '$parquimetro' } "
            + "} }",
        "{ '$sort': { 'totalTickets': -1 } }",
        "{ '$group': { "
            + "'_id': '$_id.parquimetroId', "
            + "'parquimetro': { '$first': '$parquimetro' }, "
            + "'horarioMovimentado': { '$first': '$_id.horario' }, "
            + "'totalTickets': { '$first': '$totalTickets' } "
            + "} }"
      })
  Slice<BusyHoursDTO> buscarHorarioMaisMovimentado(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
