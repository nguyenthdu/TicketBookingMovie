package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ReturnInvoiceDto {
    private String code;
    private String reason;
    private LocalDateTime cancelDate;
    private Long invoiceId;
    private String invoiceCode;
    private LocalDateTime invoiceDate;
    private String userCode;
    private String userName;
    private int quantity;
    private BigDecimal total;
}
