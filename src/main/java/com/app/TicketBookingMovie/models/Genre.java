package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "genre", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code"),
        @UniqueConstraint(columnNames = "name")
}
)
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @NotEmpty(message = "Name is not empty")
    private String name;
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies;
    private LocalDateTime createdDate= LocalDateTime.now();
    public Genre() {
    }
}
