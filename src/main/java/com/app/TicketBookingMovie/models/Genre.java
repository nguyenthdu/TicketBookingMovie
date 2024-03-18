package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

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
    //    @ManyToMany
//    @JoinTable(name = "movie_genre",
//            joinColumns = @JoinColumn(name = "genre_id"),
//            inverseJoinColumns = @JoinColumn(name = "movie_id"))
//    private Set<Movie> movies;
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies;

    public Genre(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Genre() {
    }
}
