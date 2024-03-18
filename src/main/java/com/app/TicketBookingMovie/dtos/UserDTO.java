package com.app.TicketBookingMovie.dtos;

import com.app.TicketBookingMovie.models.Role;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UserDTO {
	private Long id;
	private String code;
	private String username;
	private String email;
	private boolean gender;
	private LocalDate birthday;
	private String phone;
	private Set<Role> roles;
	private boolean enabled;
	private LocalDate createdDate;
}
