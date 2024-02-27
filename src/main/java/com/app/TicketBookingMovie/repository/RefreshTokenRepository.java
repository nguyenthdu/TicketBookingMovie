package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.RefreshToken;
import com.app.TicketBookingMovie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	Optional<RefreshToken> findByToken(String token);
	
	@Modifying
	int deleteByUser(User user);
}
