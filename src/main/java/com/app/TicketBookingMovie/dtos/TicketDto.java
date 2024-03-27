package com.app.TicketBookingMovie.dtos;

import lombok.Data;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.Ticket}
 */
@Data
public class TicketDto  {
    private Long id;
    private String code;
    private Long showTimeId;
    private Long seatIds;
    private double price;
}