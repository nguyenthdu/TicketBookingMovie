package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
	UserDetails loadUserByUsername(String email);
	
	List<UserDTO> getAllUsers(Integer page, Integer size,String code,String username, String phone,  String email);

	void deleteUser(Long id);
	
	UserDTO updateUser(Long id, UserDTO userDTO);

	String randomCode();
}
