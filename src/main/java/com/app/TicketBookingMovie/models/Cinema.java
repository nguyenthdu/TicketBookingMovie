package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cinema")
public class Cinema {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @NotEmpty
    private String name;
    private int totalRoom;
    @OneToOne(cascade = CascadeType.REMOVE) // Add CascadeType.REMOVE here
    private Address address;
//    @OneToMany(mappedBy = "cinema")
//    private Set<Room> rooms;
//    @ManyToMany(mappedBy = "cinemas")
//    private Set<Movie> movies;
    private boolean status = true;
    private LocalDateTime   createdDate;

    public Cinema() {
    }

}
