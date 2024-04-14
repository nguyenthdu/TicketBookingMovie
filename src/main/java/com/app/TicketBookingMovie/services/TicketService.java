package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.models.Ticket;

import java.util.List;
import java.util.Set;

public interface TicketService {
    List<Ticket> createTickets(Long showTimeId, Set<Long> seatIds);

    Ticket findById(Long typeSeatFreeId);
}
