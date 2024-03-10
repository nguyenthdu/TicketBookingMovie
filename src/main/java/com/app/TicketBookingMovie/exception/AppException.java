package com.app.TicketBookingMovie.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public class AppException extends RuntimeException {
	private final String message;
	private final int status;
	private final String timestamp;
	
	public AppException(String message, HttpStatus status, String timestamp) {
		this.message = message;
		this.status = status.value();
		this.timestamp = Instant.now().toString();
	}
	
	public AppException(String message, HttpStatus status) {
		this(message, status, Instant.now().toString());
	}
}
