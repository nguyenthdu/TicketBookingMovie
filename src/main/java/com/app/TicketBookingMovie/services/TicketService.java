package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.TicketDto;
import com.app.TicketBookingMovie.exception.AppException;

public interface TicketService {
    void createTickets(TicketDto ticketDto) throws AppException;

}
