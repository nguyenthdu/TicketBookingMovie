package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
@Data
public class PromotionLineDto {
    private Long id;
    private String code;
    private String name;
    private String image;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String applicableObject;
    private String  typePromotion;
    private Long usePerUser;
    private Long usePerPromotion;
    private Long  promotionId;
    private Set<PromotionDetailDto> promotionDetailDtos;
    private boolean status;
}
