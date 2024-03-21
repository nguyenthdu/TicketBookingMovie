package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sale_price_detail")
public class SalePriceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private double price;
    private double discount;
    private ETypeDiscount typeDiscount;
    @OneToOne
    private TypeSeat typeSeat;
    @OneToOne
    private Food food;
    @ManyToOne
    private SalePriceHeader salePriceHeader;

}
