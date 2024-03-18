package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeSeat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "seat_type")
public class TypeSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Enumerated(EnumType.STRING)
    private ETypeSeat name;
    @NotNull
    private double price;

    public TypeSeat() {
    }

    public TypeSeat(ETypeSeat name, double price) {
        this.name = name;
        this.price = price;
    }
}
