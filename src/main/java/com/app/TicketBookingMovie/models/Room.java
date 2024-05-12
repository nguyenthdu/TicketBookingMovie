package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ETypeRoom;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private  String code;
    private String name;
    @Enumerated(EnumType.STRING)
    private ETypeRoom type;
    private int totalSeats;
    @ManyToOne
    private Cinema cinema;
    @OneToMany
    private Set<Seat> seats;
    @OneToMany(mappedBy = "room")
    private Set<PriceDetail> priceDetails;
    private boolean status = true;
    private LocalDateTime createdDate ;
    public Room() {
    }
}
