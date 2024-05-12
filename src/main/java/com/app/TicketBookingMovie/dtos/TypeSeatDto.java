package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TypeSeatDto   {
    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private boolean active_price;
}
