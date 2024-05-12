package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class InvoiceFoodDetailDto   {
    private Long id;
    private String foodName;
    private int quantity;
    private double price;
    private double priceItem;
    private String note;
}
