package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PriceDetailDto {
    private Long id;
    private double price;
    private Long priceHeaderId;
    private boolean status;
    private LocalDateTime createdDate;
}
