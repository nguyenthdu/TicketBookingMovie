package com.app.TicketBookingMovie.payload.request;

import lombok.Data;

@Data
public class SigninRequest {
	private String email;
	private String password;
}