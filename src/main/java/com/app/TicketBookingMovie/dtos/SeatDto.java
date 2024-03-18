package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class SeatDto {
    private Long id;
    private String code;
    private String type;
    private String status;
    private String room;
    private String movie;
    private String showtime;
    private String user;
}
