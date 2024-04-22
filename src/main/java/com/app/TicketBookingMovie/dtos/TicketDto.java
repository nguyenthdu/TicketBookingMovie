package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.Ticket}
 */
@Data
public class TicketDto  {
    private Long id;
    private String code;
    private Long showTimeId;
    private Set<Long> seatIds;
    private LocalDateTime createdDate;
}