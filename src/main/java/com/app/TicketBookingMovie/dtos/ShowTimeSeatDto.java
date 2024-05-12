package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class ShowTimeSeatDto   {
    private Long id;
    private SeatDto seat;
    private Long showTimeId;
    private boolean status;
private boolean hold;
}
