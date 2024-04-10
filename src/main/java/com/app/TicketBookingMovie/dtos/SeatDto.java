package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeatDto {
    private Long id;
    private String code;
    private String name;
    private int seatRow;
    private int seatColumn;
    private boolean status;
    private BigDecimal price;
    private Long seatTypeId;

}
