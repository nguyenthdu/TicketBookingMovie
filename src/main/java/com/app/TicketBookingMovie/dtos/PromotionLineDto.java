package com.app.TicketBookingMovie.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PromotionLineDto implements Serializable {
    private Long id;
    private String code;
    private String name;
    private String image;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String typePromotion;
    private Long promotionId;
    private PromotionDiscountDetailDto promotionDiscountDetailDto;
    private PromotionFoodDetailDto promotionFoodDetailDto;
    private PromotionTicketDetailDto promotionTicketDetailDto;
    private int quantity;
    private boolean status;
    private LocalDateTime createdAt;
}
