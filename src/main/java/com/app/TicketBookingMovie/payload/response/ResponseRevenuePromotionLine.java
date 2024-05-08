package com.app.TicketBookingMovie.payload.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ResponseRevenuePromotionLine {
    private String code;
    private String name;
    private String image;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String promotionType;
    private String promotionCode;
    private String promotionName;
    private int promotionQuantity;
    private BigDecimal promotionValue;
    private String  valueType;
    private int totalQuantity;
    private int quantityUsed;
    private int quantityNotUsed;

}
