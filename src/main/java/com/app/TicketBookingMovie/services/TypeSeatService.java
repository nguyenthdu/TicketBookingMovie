package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.models.TypeSeat;

import java.util.Set;

public interface TypeSeatService {
    Set<TypeSeat> createTypeSeat();

    TypeSeatDto getTypeSeatById(Long id);

    TypeSeatDto updateTypeSeatById(TypeSeatDto typeSeatDto);



    Set<TypeSeatDto> getAllTypeSeats();
}
