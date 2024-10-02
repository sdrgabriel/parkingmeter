package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {
    @Query("{ 'status_pagamento': 'PENDENTE', 'veiculo._id': ?0 }")
    Optional<Ticket> findPendingTicketByVehicleId(String vehicleId);

    @Query("{ 'status_pagamento': 'PENDENTE', 'parquimetro._id': ?0 }")
    List<Ticket> findPendingTicketsByParkingMeterId(String parkingMeterId);
}
