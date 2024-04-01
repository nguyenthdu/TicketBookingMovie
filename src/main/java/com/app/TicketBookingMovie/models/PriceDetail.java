package com.app.TicketBookingMovie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "price_detail")
public class PriceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double price;
    @ManyToOne
    private TypeSeat typeSeat;
    @ManyToOne
    private Food food;
    @JsonIgnore
    @ManyToOne
    private PriceHeader priceHeader;
    private boolean status;
    private LocalDateTime createdDate;

}
