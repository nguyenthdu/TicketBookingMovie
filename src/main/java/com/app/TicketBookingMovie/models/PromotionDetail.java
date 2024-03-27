package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table
public class PromotionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ETypeDiscount typeDiscount;
    private int discountValue;
    private int discountPercent;
    private int maxDiscountValue;
    private int minBillValue;
    @ManyToOne
    private Food food;
    @JsonIgnore
    @ManyToOne
    private PromotionLine promotionLine;
}
