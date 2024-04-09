package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.models.TypeSeat;

import java.util.Set;

public interface TypeSeatService {
    void createTypeSeat();
    TypeSeatDto getTypeSeatById(Long id);
    TypeSeat findById(Long id);

    void updateTypeSeatById(TypeSeatDto typeSeatDto);

    Set<TypeSeatDto> getAllTypeSeats();

}
