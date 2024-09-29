package com.postech.fiap.parkingmeter.domain.repository;

import com.postech.fiap.parkingmeter.domain.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {}
