package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "promotion_food_detail")
public class PromotionFoodDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long foodRequired;//đồ ăn cần mua
    private int quantityRequired;//số lượng cần mua
    private Long foodPromotion;//đồ ăn được tặng
    private int quantityPromotion;//số lượng được tặng
    private BigDecimal price;//giá sản phẩm được tặng
}
