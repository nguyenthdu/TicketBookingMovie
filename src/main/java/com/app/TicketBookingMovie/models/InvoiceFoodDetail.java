package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "invoice_food_detail")
public class InvoiceFoodDetail  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Food food;
    private int quantity;
    private BigDecimal price;
    private String note;
}
