package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
	
	UserDetails loadUserByUsername(String email);
	  List<UserDTO> getAllUsers(Integer page, Integer size, String phone, Long code, String email);
	
  UserDTO getUserByCode(Long code);
  UserDTO getUserByUsername(String username);
  UserDTO getUserByEmail(String email);
  UserDTO getUserByPhone(String phone);
  void deleteUser(Long id);
  UserDTO updateUser( Long id, UserDTO userDTO);

  Long randomCode();
	
	
}
