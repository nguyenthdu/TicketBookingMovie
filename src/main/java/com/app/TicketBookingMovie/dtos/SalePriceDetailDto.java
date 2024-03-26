package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SalePriceDetailDto {
    private Long id;
    private double priceDecrease = 0;
    private double discount;
    private String typeDiscount;
    private Long typeSeatId;
    private Long foodId;
    private Long salePriceId;
    private boolean status;
    private LocalDateTime createdDate;
}
