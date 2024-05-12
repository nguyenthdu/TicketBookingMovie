package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "price_header")
public class PriceHeader   {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String name;
    private String description;
    private LocalDateTime startDate;

    private LocalDateTime endDate;
    private boolean status;
    @OneToMany(mappedBy = "priceHeader")
    private Set<PriceDetail> priceDetails;

    private LocalDateTime createdDate;

}
