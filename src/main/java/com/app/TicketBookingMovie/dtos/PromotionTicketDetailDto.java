package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotionTicketDetailDto   {
    private  Long id;
    private Long typeSeatRequired;
    private int quantityRequired;
    private Long typeSeatPromotion;
    private int quantityPromotion;
    private BigDecimal price;


}
