package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PromotionLineDto {
    private Long id;
    private String code;
    private String name;
    private String image;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String  typePromotion;
    private Long  promotionId;
    private PromotionDiscountDetailDto promotionDiscountDetailDto;
    private PromotionFoodDetailDto promotionFoodDetailDto;
    private PromotionTicketDetailDto promotionTicketDetailDto;
    private int quantity;
    private boolean status;
    private LocalDateTime createdAt;
}
