package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.Room}
 */
@Data
public class RoomDto   {
    private Long id;
    private String code;
    private String name;
    private double price;
    private String type;
    private int totalSeats;
    private Long cinemaId;
    private boolean status = true;
    private Set<SeatDto> seats;
    private LocalDateTime createdDate;
}