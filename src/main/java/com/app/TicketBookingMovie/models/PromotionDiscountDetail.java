package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "promotion_detail")
public class PromotionDiscountDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ETypeDiscount typeDiscount;
    private BigDecimal discountValue;
    private int maxValue;
    private BigDecimal minBillValue;

}
