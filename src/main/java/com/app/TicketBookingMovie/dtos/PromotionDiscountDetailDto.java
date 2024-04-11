package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class PromotionDetailDto {
    private Long id;
    private String typeDiscount;
    private double discountValue;
    private int maxValue;
    private double minBillValue;
    private Long foodId;
    private Long promotionLineId;
}
