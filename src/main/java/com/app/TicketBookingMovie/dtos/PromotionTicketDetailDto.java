package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionTicketDetailDto {
    private  Long id;
    private Long typeSeatRequired;
    private int quantityRequired;
    private Long typeSeatFree;
    private int quantityFree;
    private BigDecimal price;


}
