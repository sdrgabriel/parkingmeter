package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.ParkingMeter;
import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import com.postech.fiap.parkingmeter.domain.model.dto.RankedParkingMeterDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
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

  Page<Ticket> findByStatusPagamento(Ticket.StatusPagamento status, Pageable pageable);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'horarioInicio': { '$gte': ?0, '$lt': ?1 } } }",
        "{ '$group': { '_id': { 'parquimetroId': '$parquimetro.id', 'horario': { '$dateToString': { 'format': '%Y-%m-%d %H', 'date': '$horarioInicio' } } }, 'totalTickets': { '$sum': 1 } } }",
        "{ '$sort': { 'totalTickets': -1 } }",
        "{ '$group': { '_id': '$_id.parquimetroId', 'horarioMovimentado': { '$first': '$_id.horario' }, 'totalTickets': { '$first': '$totalTickets' } } }"
      })
  Slice<BusyHoursDTO> buscarHorarioMaisMovimentado(
      LocalDate startDate, LocalDate endDate, Pageable pageable);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'horario_inicio': { '$gte': ?0, '$lt': ?1 } } }",
              "{ '$project': { parquimetroId: '$parquimetro.id', 'data': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$horario_inicio' } }, 'totalArrecadado': { '$sum': '$valor_total_cobrado' } } }",
        "{ '$group': { '_id': '$parquimetro.id', 'data': { '$dateToString': { 'format': '%Y-%m-%d', 'date': '$horario_inicio' } }, 'totalArrecadado': { '$sum': '$valor_total_cobrado' } } }",
        "{ '$sort': { 'totalArrecadado': -1 } }",
        "{ '$group': { '_id': '$_id.data', 'parquimetros': { '$push': { 'parquimetroId': '$_id.parquimetroId', 'totalArrecadado': '$totalArrecadado' } } } }"
      })
  Slice<RankedParkingMeterDTO> rankParquimetrosPorArrecadacaoPorDia(
      LocalDate startDate, LocalDate endDate, Pageable pageable);
}
