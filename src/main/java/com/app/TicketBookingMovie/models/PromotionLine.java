package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.EApplicableObject;
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
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    @Enumerated(EnumType.STRING)
    private EApplicableObject applicableObject;
    @Enumerated(EnumType.STRING)
    private ETypePromotion typePromotion;
    private Long usePerUser;
    private Long usePerPromotion;
    @OneToOne
    private PromotionDetail promotionDetail;
    @ManyToOne
    @JoinColumn(name = "promotion_id")
    @JsonIgnore
    private Promotion promotion;
    private boolean status;
}
