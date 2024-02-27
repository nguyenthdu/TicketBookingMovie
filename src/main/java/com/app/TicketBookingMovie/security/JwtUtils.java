package com.app.TicketBookingMovie.security;



import com.app.TicketBookingMovie.models.User;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Date;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
	@Value("${movie.app.jwtSecret}")
	private String jwtSecret;
	@Value("${movie.app.jwtExpirationMs}")
	private int jwtExpirationMs;
	@Value("${movie.app.jwtCookieName}")
	private String jwtCookie;
	@Value("${movie.app.jwtRefreshCookieName}")
	private String jwtRefreshCookie;
	
	public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
		String jwt = generateTokenFromEmail(userPrincipal.getEmail());
		return generateCookie(jwtCookie, jwt, "/api");
	}
	
	public ResponseCookie generateJwtCookie(User user) {
		String jwt = generateTokenFromEmail(user.getEmail());
		return generateCookie(jwtCookie, jwt, "/api");
	}
	
	public ResponseCookie generateRefreshJwtCookie(String refreshToken) {
		return generateCookie(jwtRefreshCookie, refreshToken, "/api/auth/refreshtoken");
	}
	
	public String getJwtFromCookies(HttpServletRequest request) {
		return getCookieValueByName(request, jwtCookie);
	}
	
	public String getJwtRefreshFromCookies(HttpServletRequest request) {
		return getCookieValueByName(request, jwtRefreshCookie);
	}
	
	public ResponseCookie getCleanJwtCookie() {
		ResponseCookie cookie = ResponseCookie.from(jwtCookie, null).path("/api").build();
		return cookie;
	}
	
	public ResponseCookie getCleanJwtRefreshCookie() {
		ResponseCookie cookie = ResponseCookie.from(jwtRefreshCookie, null).path("/api/auth/refreshtoken").build();
		return cookie;
	}
	
	public String getEmailFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}
	
	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		}
		return false;
	}
	
	public String generateTokenFromEmail(String email) {
		return Jwts.builder().setSubject(email).setIssuedAt(new Date()).setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)).signWith(SignatureAlgorithm.HS256, jwtSecret).compact();
	}
	
	private ResponseCookie generateCookie(String email, String value, String path) {
		ResponseCookie cookie = ResponseCookie.from(email, value).path(path).maxAge(24 * 60 * 60).httpOnly(true).build();
		return cookie;
	}
	
	private String getCookieValueByName(HttpServletRequest request, String name) {
		Cookie cookie = WebUtils.getCookie(request, name);
		if(cookie != null) {
			return cookie.getValue();
		} else {
			return null;
		}
	}
}