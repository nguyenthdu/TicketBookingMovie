package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByCodeContaining(String code, Pageable pageable);

    Page<Movie> findByNameContaining(String name, Pageable pageable);

    //find by genre id
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = ?1")
    Page<Movie> findByGenreId(Long id, Pageable pageable);

    @Query("SELECT m FROM Movie m JOIN m.cinemas c WHERE c.id = ?1")
    Page<Movie> findByCinemaId(Long cinemaId, Pageable pageable);

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.genres g WHERE g.id = ?1")
    long countByGenreId(Long genreId);

    @Query("SELECT COUNT(m) FROM Movie m JOIN m.cinemas c WHERE c.id = ?1")
    long countByCinemaId(Long cinemaId);
}
