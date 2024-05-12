package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceTicketDetailDto   {
    private Long id;
    private String  ticketCode;
    private String seatName;
   private String rowCol;
    private String seatType;
    private int quantity;
    private BigDecimal price;
    private BigDecimal priceItem;
    private String note;
}
