package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for {@link com.app.TicketBookingMovie.models.ShowTime}
 */
@Data
public class ShowTimeDto {
    private Long id;
    private String code;
    private LocalDate showDate;
    private LocalTime showTime;
    private Long movieId;
    private Long roomId;
    boolean status;
    private int seatsBooked;
}