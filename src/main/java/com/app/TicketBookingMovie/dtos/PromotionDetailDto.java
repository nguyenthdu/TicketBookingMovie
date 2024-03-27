package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class PromotionDetailDto {
    private Long id;
    private String typeDiscount;
    private int discountValue;
    private int discountPercent;
    private int maxDiscountValue;
    private int minBillValue;

    private Long foodId;

    private Long promotionLineId;
}
