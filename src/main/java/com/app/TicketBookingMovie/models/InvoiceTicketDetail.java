package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "invoice_ticket_detail")
public class InvoiceTicketDetail  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    private Ticket ticket;
    private int quantity;
    private BigDecimal price;
    private String note;
}
