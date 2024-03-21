package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class SalePriceDetailDto {
    private Long id;
    private String code;
    private double price;
    private double discount;
    private String typeDiscount;
    private Long typeSeatId;
    private Long foodId;
    private Long salePriceHeaderId;
}
