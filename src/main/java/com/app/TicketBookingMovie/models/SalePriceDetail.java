package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private double priceDecrease;
    private double discount;
    @Enumerated(EnumType.STRING)
    private ETypeDiscount typeDiscount = ETypeDiscount.AMOUNT;
    @OneToOne
    private TypeSeat typeSeat;
    @OneToOne
    private Food food;
    @JsonIgnore
    @ManyToOne
    private SalePrice salePrice;
    private boolean status;

}
