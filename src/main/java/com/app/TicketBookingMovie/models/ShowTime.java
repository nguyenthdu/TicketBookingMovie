package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "show_time")
public class ShowTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private LocalDate showDate;
    private LocalDateTime showTime;
    @ManyToOne
    private Movie movie;
    @ManyToOne
    private Room room;
    @ManyToOne
    private Cinema cinema;
    private boolean status;

    public ShowTime() {
    }

    public ShowTime(Long id) {
        this.id = id;
    }

    public ShowTime(LocalDate showDate, LocalDateTime showTime, Movie movie, Room room, Cinema cinema, boolean status) {
        this.showDate = showDate;
        this.showTime = showTime;
        this.movie = movie;
        this.room = room;
        this.cinema = cinema;
        this.status = status;
    }
}
