package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvoiceDto {
    private  Long id;
    private String code;
    private double totalPrice;
    private LocalDateTime createdDate;
    private boolean status;
    private String showTimeCode;
    private String cinemaName;
    private String roomName;
    private String movieImage;
    private String movieName;
    private String userName;
    private String staffName;

}
