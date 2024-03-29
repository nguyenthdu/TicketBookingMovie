package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.SignupDto;
import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.models.User;
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
	void updateUserProfile(String email, UserDto userDTO);
	void createGuest();
	void createUserInTicket(UserDto userDto);

	void createUser(SignupDto signupDto);
	void createMor(SignupDto SignupDto);
	void createRoles();
	void createAdmin();
    long countUsers(String code, String username, String phone, String email, Long roleId);
}
