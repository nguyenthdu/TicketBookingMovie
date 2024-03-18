package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
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
    @NotEmpty
    private int totalRoom;
    @OneToOne(cascade = CascadeType.REMOVE) // Add CascadeType.REMOVE here
    private Address address;
    @OneToMany
    private Set<Room> rooms;
    private boolean status = true;
    public Cinema() {
    }

    public Cinema(String name, int totalRoom, Address address, boolean status) {
        this.name = name;
        this.totalRoom = totalRoom;
        this.address = address;
        this.status = status;
    }
}
