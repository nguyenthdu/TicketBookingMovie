package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class TypeSeatDto {
    private Long id;
    private String code;
    private String name;
    private  double price;
}
