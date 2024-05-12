package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ticket")
public class Ticket   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @ManyToOne
    private Seat seat;
    @ManyToOne
    private ShowTime showTime;
    private LocalDateTime createdDate = LocalDateTime.now();
}
