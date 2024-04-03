package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.RefreshToken;
import com.app.TicketBookingMovie.repository.RefreshTokenRepository;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
	@Value("${movie.app.jwtRefreshExpirationMs}")
	private Long refreshTokenDurationMs;

	private final RefreshTokenRepository refreshTokenRepository;

	private final UserRepository userRepository;
	public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.userRepository = userRepository;
	}
	
	@Override
	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}
	
	@Override
	public RefreshToken createRefreshToken(Long userId) {
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(userRepository.findById(userId).get());
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
		refreshToken.setToken(UUID.randomUUID().toString());
		refreshToken = refreshTokenRepository.save(refreshToken);
		return refreshToken;
	}
	
	@Override
	public RefreshToken verifyExpiration(RefreshToken token) {
		if(token.getExpiryDate().compareTo(Instant.now()) < 0) {
			refreshTokenRepository.delete(token);
			throw new AppException("Refresh token was expired. Please make a new signin request", HttpStatus.FORBIDDEN);
		}
		return token;
	}
	
	@Override
	@Transactional
	public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }
}
