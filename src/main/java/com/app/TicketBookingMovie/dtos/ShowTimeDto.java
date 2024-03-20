package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.ShowTime}
 */
@Data
public class ShowTimeDto {
    private Long id;
    private String code;
    private LocalDate showDate;
    private LocalDateTime showTime;
    private Long movieId;
    private Long roomId;
    boolean status;
}