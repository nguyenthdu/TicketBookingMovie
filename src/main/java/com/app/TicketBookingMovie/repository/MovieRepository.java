package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByCode(String code);
    Optional<Movie> findByCode(String code);

    Page<Movie> findByCodeContaining(String code, Pageable pageable);

    Page<Movie> findByNameContaining(String name, Pageable pageable);

    //find by genre id
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = ?1")
    Page<Movie> findByGenreId(Long id, Pageable pageable);

}
