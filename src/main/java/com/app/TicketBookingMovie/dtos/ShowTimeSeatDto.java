package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class ShowTimeSeatDto {
    private Long id;
    private Long seatId;
    private Long showTimeId;
    private boolean status;
}
