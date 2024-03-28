package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "show_time")
public class ShowTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @Past
    private LocalDate showDate;
    private LocalTime showTime;
    @ManyToOne
    private Movie movie;
    @ManyToOne
    private Room room;
    private boolean status;
    private int seatsBooked;
    private LocalDateTime createdDate;
    @OneToMany(mappedBy = "showTime", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShowTimeSeat> showTimeSeat;
    public ShowTime() {
    }

}
