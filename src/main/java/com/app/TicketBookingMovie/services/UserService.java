package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.SignupDto;
import com.app.TicketBookingMovie.dtos.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
	UserDetails loadUserByUsername(String email);
	
	List<UserDto> getAllUsersPage(Integer page, Integer size, String code, String username, String phone, String email, Long roleId);

	void deleteUser(Long id);
	
	UserDto updateUser(Long id, UserDto userDTO);

	void createUser(SignupDto signupDto);
	void createMor(SignupDto SignupDto);
	void createAdmin();

    long countUsers(String code, String username, String phone, String email, Long roleId);
}
