package com.app.TicketBookingMovie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ShowTimeSeat  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Seat seat;
    @ManyToOne
    @JsonIgnore
    private ShowTime showTime;
    private boolean status;
    private boolean hold;
}
