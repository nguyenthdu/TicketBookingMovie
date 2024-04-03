package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.models.RefreshToken;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenService {
	Optional<RefreshToken> findByToken(String token);
	
	RefreshToken createRefreshToken(Long userId);
	
	RefreshToken verifyExpiration(RefreshToken token);
	
	@Transactional
	void deleteByUserId(Long userId);
}
