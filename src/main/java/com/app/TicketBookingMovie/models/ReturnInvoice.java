package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "return_invoice")
public class ReturnInvoice {
    @Id
    @GeneratedValue
    private int id;
    private String code;
    private String reason;
    private LocalDateTime returnDate;
    @OneToOne
    private Invoice invoice;

}
