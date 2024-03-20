package com.app.TicketBookingMovie.models;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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


    public Seat() {
    }

    public Seat(int seatRow, int seatColumn, boolean status, TypeSeat seatType) {
        this.seatRow = seatRow;
        this.seatColumn = seatColumn;
        this.status = status;
        this.seatType = seatType;
    }
}
