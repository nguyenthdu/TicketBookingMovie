package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.EApplicableObject;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

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
    private EApplicableObject applicableObject;
    private ETypePromotion typePromotion;
    private int usePerUser;
    private int usePerPromotion;
    @ManyToOne
    private Promotion promotion;
    @OneToMany
    private Set<PromotionDetail> promotionDetails;
    private boolean status;
}
