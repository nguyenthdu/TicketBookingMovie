package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "promotion_detail")
public class PromotionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ETypeDiscount typeDiscount;
    private double discountValue;
    private int maxValue;
    private double minBillValue;
    @ManyToOne
    private Food food;
    @OneToOne(mappedBy = "promotionDetail")
    @JsonIgnore
    private PromotionLine promotionLine;
}
