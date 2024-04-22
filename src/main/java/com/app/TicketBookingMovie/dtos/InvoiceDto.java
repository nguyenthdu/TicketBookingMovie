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



//    private String code;
//    private String userName;
//    private String emailUser;
//    private String phoneUser;
//    private String staff;
//    private String movie;
//    private String showtime;
//
//    private String cinema;
//    private String room;
//
//    private String seat;
//     private String food;
//    private double price;
//    private double promotion;
//    private double totalPrice;
//    private LocalDateTime createdDate;
//    private LocalDateTime cancelledDate;
//    private List<InvoiceDetailDto> invoiceDetails;

}
