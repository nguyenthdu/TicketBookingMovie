package com.app.TicketBookingMovie.models;


import com.app.TicketBookingMovie.models.enums.ESize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "food")
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String image;
    @ManyToOne
    private PriceDetail price;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private ESize size;
    @ManyToOne
    @JoinColumn(name = "category_food_id")
    private CategoryFood categoryFood;
    private boolean status;
    private LocalDateTime createdDate;
    public Food() {
    }
}
