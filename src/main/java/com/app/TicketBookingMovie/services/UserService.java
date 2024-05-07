package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.payload.request.SignupRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
	UserDetails loadUserByUsername(String email);
	//getCurrentUser
	User getCurrentUser(String email);
	User findById(Long id);
	List<UserDto> getAllUsersPage(Integer page, Integer size, String code, String username, String phone, String email, Long roleId);
	UserDto getUserById(Long id);
	void deleteUser(Long id);
	void updateUser(Long id, UserDto userDTO);
	UserDto createGuest();
	void createUserInTicket(UserDto userDto);

	void createUser(SignupRequest signupRequest);
	void createMor(SignupRequest SignupRequest);
	void createRoles();
	void createAdmin();
    long countUsers(String code, String username, String phone, String email, Long roleId);
}
