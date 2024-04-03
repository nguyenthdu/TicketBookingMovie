package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class InvoiceTicketDetailDto {
    private Long id;
    private String  ticketCode;
    private String seatName;
   private String rowCol;
    private String seatType;
    private int quantity;
    private double price;
    private double priceItem;
    private String note;
}
