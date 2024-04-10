package com.app.TicketBookingMovie.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "seat")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private int seatRow;
    private int seatColumn;
    private boolean status;
    @ManyToOne
    private TypeSeat seatType;
    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShowTimeSeat> showTimeSeats = new HashSet<>();



    public Seat() {
    }

}
