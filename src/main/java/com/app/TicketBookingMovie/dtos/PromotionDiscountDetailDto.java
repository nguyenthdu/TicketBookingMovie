package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class PromotionDiscountDetailDto   {
    private Long id;
    private String typeDiscount;
    private double discountValue;
    private int maxValue;
    private double minBillValue;
}
