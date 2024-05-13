package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Ticket findByShowTimeId(Long showTimeId);

}
