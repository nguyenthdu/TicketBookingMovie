package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genres g WHERE g.id = ?1")
    long countByGenreId(Long genreId);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.cinemas c WHERE c.id = ?1")
    long countByCinemaId(Long cinemaId);

    long countByCinemasIdAndNameContaining(Long cinemaId, String name);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genres g WHERE g.id = ?2 AND m.id = ?1")
    long countByCinemasIdAndGenreId(Long cinemaId, Long genreId);
}
