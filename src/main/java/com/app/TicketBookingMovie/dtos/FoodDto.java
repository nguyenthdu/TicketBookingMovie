package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodDto {
    private Long id;
    private String code;
    private String name;
    private String image;
    private PriceDetailDto priceDetail;
    private int quantity;
    private String size;
    private Long categoryId;
    private String categoryName;
    private boolean status;
    private LocalDateTime createdDate;
}
