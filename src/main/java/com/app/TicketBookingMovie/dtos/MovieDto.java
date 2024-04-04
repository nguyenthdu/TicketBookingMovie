package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class MovieDto {
    private Long id;
    private String code;
    private String name;
    private String imageLink;
    private String trailerLink;
    private String description;
    private int durationMinutes;
    private LocalDate releaseDate;
    private String country;
    private String director;
    private String cast;
    private String producer;
    private Set<Long> genreIds;
    private Set<Long> cinemaIds;
    private boolean status;
    private LocalDateTime createdDate;
    private List<GenreDto> genres;
}

