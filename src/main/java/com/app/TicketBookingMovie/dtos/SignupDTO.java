package com.app.TicketBookingMovie.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
@Data
public class SignupDTO {
	
	private Integer code;
	private String username;
	private String email;
	private boolean gender;
	private LocalDate birthday;
	private String phone;
	private String password;
	private Set<String> roles = new HashSet<>();
	

}