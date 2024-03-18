package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeRoom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private  String code;
    @Embedded
    private ETypeRoom type;
    private int totalSeats;
    @ManyToOne
    private Cinema cinema;
    @OneToMany
    private Set<Seat> seats;
    private boolean status;
    public Room() {
    }

    public Room(ETypeRoom type, int totalSeats, Cinema cinema, Set<Seat> seats, boolean status) {
        this.type = type;
        this.totalSeats = totalSeats;
        this.cinema = cinema;
        this.seats = seats;
        this.status = status;
    }
}
