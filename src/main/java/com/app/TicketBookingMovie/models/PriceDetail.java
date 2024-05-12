package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.EDetailType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "price_detail")
public class PriceDetail   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private EDetailType type; //  'FOOD', 'ROOM' or 'TYPE_SEAT'
    @ManyToOne
    @JsonIgnore
    private Food food;
    @ManyToOne
    @JsonIgnore
    private Room room;
    @ManyToOne
    @JsonIgnore
    private TypeSeat typeSeat;
    @JsonIgnore
    @ManyToOne
    private PriceHeader priceHeader;
    private boolean status;
    private LocalDateTime createdDate;
}
