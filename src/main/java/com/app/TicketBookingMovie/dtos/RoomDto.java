package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.util.Set;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.Room}
 */
@Data
public class RoomDto   {
    private Long id;
    private String code;
    private String name;
    private String type;
    private int totalSeat;
    private Long cinemaId;
    private boolean status = true;
    private Set<SeatDto> seats;
}