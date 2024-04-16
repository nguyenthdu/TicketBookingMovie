package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReturnInvoiceDto {
    private String code;
    private String reason;
    private LocalDateTime cancelDate;
    private Long invoiceId;
}
