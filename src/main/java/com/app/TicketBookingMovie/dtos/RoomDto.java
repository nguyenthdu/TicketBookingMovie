package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.Room}
 */
@Data
public class RoomDto    {
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private boolean active_price;
    private String type;
    private int totalSeats;
    private String cinemaName;
    private Long cinemaId;
    private boolean status = true;
    private Set<SeatDto> seats;
    private LocalDateTime createdDate;
}