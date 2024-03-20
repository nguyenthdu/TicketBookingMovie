package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.SeatDto;

public interface SeatService {
    SeatDto createSeat(SeatDto seatDto);
    SeatDto getSeatById(Long id);

    void deleteSeatById(Long id);

}
