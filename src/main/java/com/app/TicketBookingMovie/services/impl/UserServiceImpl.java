package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.UserDTO;
import com.app.TicketBookingMovie.exception.ErrorMessage;
import com.app.TicketBookingMovie.models.ERole;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
	UserRepository userRepository;
	@Autowired
    private ModelMapper modelMapper;
	
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
		return UserDetailsImpl.build(user);
	}
	
	@Override
	public List<UserDTO> getAllUsers() {
//		 List<User> users = userRepository.findAll();
		List<User> users = userRepository.findAllByRolesName(ERole.ROLE_USER);
        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
        return userDTOs;
		
	}
	
	@Override
	public UserDTO getUserByCode(Integer code) {
		User user = userRepository.findByCode(code).orElseThrow(() -> new ErrorMessage("Not found user with code: " + code, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByUsername(String username) {
		 User user = userRepository.findByUsername(username).orElseThrow(() -> new ErrorMessage("Not found user with username: " + username, HttpStatus.NOT_FOUND));
		 return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByEmail(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new ErrorMessage("Not found user with email: " + email, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByPhone(String phone) {
		User user = userRepository.findByPhone(phone).orElseThrow(() -> new ErrorMessage("Not found user with phone: " + phone, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
}
