package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
@Data
public class SignupDTO {
	private String username;
	private String email;
	private boolean gender;
	private LocalDate birthday;
	private String phone;
	private String password;
	private Set<String> roles ;
}