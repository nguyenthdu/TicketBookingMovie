package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PromotionDto   {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Set<PromotionLineDto> promotionLineDtos;
    private LocalDateTime createdAt;
    private boolean status;
}
