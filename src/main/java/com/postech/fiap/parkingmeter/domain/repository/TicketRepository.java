package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.Ticket;
import com.postech.fiap.parkingmeter.domain.model.dto.BusyHoursDTO;
import com.postech.fiap.parkingmeter.domain.model.enums.PaymentStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

  @Query("{ 'payment_status': 'PENDING', 'vehicle._id': ?0 }")
  Optional<Ticket> findPendingTicketByVehicleId(String vehicleId);

  @Query("{ 'payment_status': 'PENDING', 'parkingMeter._id': ?0 }")
  List<Ticket> findPendingTicketsByParkingMeterId(String parkingMeterId);

  List<Ticket> findByVehicleLicensePlate(String licensePlate);

  Page<Ticket> findByStartTimeBetween(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  Page<Ticket> findByPaymentStatus(PaymentStatusEnum status, Pageable pageable);

  @Aggregation(
      pipeline = {
        "{ '$match': { 'start_time': { '$gte': ?0, '$lt': ?1 } } }",
        "{ '$group': { "
            + "'_id': { "
            + "'parkingMeterId': '$parkingMeter._id', "
            + "'hour': { '$hour': '$start_time' } "
            + "}, "
            + "'totalTickets': { '$sum': 1 }, "
            + "'parkingMeter': { '$first': '$parkingMeter' } "
            + "} }",
        "{ '$sort': { 'totalTickets': -1 } }",
        "{ '$group': { "
            + "'_id': '$_id.parkingMeterId', "
            + "'parkingMeter': { '$first': '$parkingMeter' }, "
            + "'operatedTime': { '$first': '$_id.hour' }, "
            + "'totalTickets': { '$first': '$totalTickets' } "
            + "} }"
      })
  Slice<BusyHoursDTO> findBusiestHour(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
