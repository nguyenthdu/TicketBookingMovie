package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
	
	
	Optional<Genre> findByName(String name);
	Optional<Genre> findById(Long id);

    long countByCodeContaining(String code);

	long countByNameContaining(String name);
}
