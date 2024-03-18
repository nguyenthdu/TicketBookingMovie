package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
	UserDetails loadUserByUsername(String email);
	
	List<UserDto> getAllUsersPage(Integer page, Integer size, String code, String username, String phone, String email);

	void deleteUser(Long id);
	
	UserDto updateUser(Long id, UserDto userDTO);

	String randomCode();
}
