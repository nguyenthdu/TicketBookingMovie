package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeSeat;
import jakarta.persistence.*;
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
    public TypeSeat() {
    }


}
