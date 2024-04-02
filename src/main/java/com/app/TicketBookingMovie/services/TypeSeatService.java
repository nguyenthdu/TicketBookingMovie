package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.TypeSeatDto;

import java.util.Set;

public interface TypeSeatService {
    void createTypeSeat();

    TypeSeatDto getTypeSeatById(Long id);

    void updateTypeSeatById(TypeSeatDto typeSeatDto);

    Set<TypeSeatDto> getAllTypeSeats();

}
