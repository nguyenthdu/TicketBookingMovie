package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeSeat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

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
    @OneToMany(mappedBy = "typeSeat")
    private Set<PriceDetail>priceDetails;
    public TypeSeat() {
    }
}
