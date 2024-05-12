package com.app.TicketBookingMovie.models;


import com.app.TicketBookingMovie.models.enums.ESize;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "food")
public class Food{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String image;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private ESize size;
    @ManyToOne
    @JoinColumn(name = "category_food_id")
    private CategoryFood categoryFood;
    @ManyToOne
    @JoinColumn(name = "cinema_id")
    @JsonIgnore
    private Cinema cinema;
   @OneToMany(mappedBy = "food")
    private Set<PriceDetail> priceDetails;
    private boolean status;
    private LocalDateTime createdDate;

    public Food() {
    }
}
