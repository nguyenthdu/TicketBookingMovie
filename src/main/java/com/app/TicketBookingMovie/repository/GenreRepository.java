package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
	
	
	Optional<Genre> findByName(String name);
	Optional<Genre> findById(Long id);
	
	Page<Genre> findByCodeContaining(String code, Pageable pageable);
	Page<Genre> findByNameContaining(String name, Pageable pageable);


}
