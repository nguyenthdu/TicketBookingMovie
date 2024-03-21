package com.app.TicketBookingMovie.models;


import com.app.TicketBookingMovie.models.enums.ESize;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private double price;
    @Enumerated(EnumType.STRING)
    private ESize size;
    @ManyToOne
    @JoinColumn(name = "category_food_id")
    private CategoryFood categoryFood;
    private boolean status;
    public Food() {
    }
}
