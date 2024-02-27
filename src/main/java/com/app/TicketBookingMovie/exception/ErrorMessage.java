package com.app.TicketBookingMovie.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public class ErrorMessage extends  RuntimeException{
	private final String message;
	private final int status;
	private final String timestamp;
	
	public ErrorMessage(String message, HttpStatus status, String timestamp) {
		this.message = message;
		this.status = status.value();
		this.timestamp = Instant.now().toString();
	}
	public ErrorMessage(String message, HttpStatus status) {
		this(message, status, Instant.now().toString());
	}
	
	
	
}
