package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class MovieDTO {
    private Long id;
    private String code;
    private String name;
    private String imageLink;
    private String trailerLink;
    private String description;
    private int durationMinutes;
    private Set<Long> genreIds;
    private LocalDate releaseDate;
    private String country;
    private String director;
    private String cast;
    private String producer;
}

