package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "promotion_ticket_detail")
public class PromotionTicketDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long typeSeatRequired;//loại ghế  cần mua
    private int quantityRequired;//số lượng cần mua
    private Long typeSeatFree;//loại ghế được tặng
    private int quantityFree;//số lượng được tặng
    private BigDecimal price;//giá sản phẩm được tặng

}
