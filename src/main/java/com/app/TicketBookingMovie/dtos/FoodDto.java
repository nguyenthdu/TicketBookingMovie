package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodDto {
    private Long id;
    private String code;
    private String name;
    private String image;
    private double price;
    private int quantity;
    private String size;
    private Long CategoryId;
    private boolean status;
    private LocalDateTime createdDate;
}
