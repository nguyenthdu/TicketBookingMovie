package com.app.TicketBookingMovie.dtos;

import com.app.TicketBookingMovie.models.Ticket;
import lombok.Data;

@Data
public class InvoiceDetailDto {
    private Long foodId;
    private Ticket ticket;
    private int quantity;
    private double price;
}
