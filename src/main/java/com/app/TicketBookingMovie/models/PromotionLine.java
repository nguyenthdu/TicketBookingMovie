package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promotion_line")
public class PromotionLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String image;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private ETypePromotion typePromotion;
    @OneToOne
    private PromotionDiscountDetail promotionDiscountDetail;
    @OneToOne
    private PromotionFoodDetail promotionFoodDetail;
    @OneToOne
    private PromotionTicketDetail promotionTicketDetail;
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    @JsonIgnore
    private Promotion promotion;
    private int quantity;
    private boolean status;
    private LocalDateTime createdAt;
}
