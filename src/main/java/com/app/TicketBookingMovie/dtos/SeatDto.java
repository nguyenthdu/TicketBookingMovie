package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class SeatDto {
    private Long id;
    private String code;
    private String name;
    private int seatRow;
    private int seatColumn;
    private boolean status;
    private Long seatTypeId;

}
