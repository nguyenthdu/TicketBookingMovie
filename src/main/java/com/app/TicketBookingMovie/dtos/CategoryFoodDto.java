package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryFoodDto {
    private Long id;
    private String code;
    private String name;
    private LocalDateTime createdDate;
}
