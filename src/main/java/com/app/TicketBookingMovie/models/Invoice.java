package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private double totalPrice;
    @ManyToOne
    private User user;
    @ManyToOne
    private InvoiceDetail invoiceDetail;
    private LocalDateTime createdDate;
    private boolean status;
}
