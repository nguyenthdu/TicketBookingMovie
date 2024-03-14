package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.UserDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.RoleRepository;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService, UserDetailsService {
	@Autowired
	private final UserRepository userRepository;
	@Autowired
	private final RoleRepository roleRepository;
	@Autowired
	private final ModelMapper modelMapper;
	
	@Override
	@Transactional
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
		return UserDetailsImpl.build(user);
	}
	
	@Override
	public List<UserDTO> getAllUsers(Integer page, Integer size, Long code,String username, String phone, String email) {
		Pageable pageable = PageRequest.of(page, size);
		Page<User> userPage;
		if(code != null && code != 0) {
			userPage = userRepository.findByCode(code, pageable);
		} else if(username != null && !username.isEmpty()) {
			userPage = userRepository.findByUsernameContaining(username, pageable);
		} else if(phone != null && !phone.isEmpty()) {
			userPage = userRepository.findByPhoneContaining(phone, pageable);
		} else if(email != null && !email.isEmpty()) {
			userPage = userRepository.findByEmailContaining(email, pageable);
		} else {
			userPage = userRepository.findAll(pageable);
		}
		List<UserDTO> userDTOs = userPage.getContent().stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toList());
		return userDTOs;
	}
	
	@Override
	public UserDTO getUserByCode(Long code) {
		User user = userRepository.findByCode(code).orElseThrow(() -> new AppException("Not found user with code: " + code, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException("Not found user with username: " + username, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByEmail(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> new AppException("Not found user with email: " + email, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public UserDTO getUserByPhone(String phone) {
		User user = userRepository.findByPhone(phone).orElseThrow(() -> new AppException("Not found user with phone: " + phone, HttpStatus.NOT_FOUND));
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public void deleteUser(Long id) {
		//xoa sẽ chuyển trạng thái user thành false
		Optional<User> user = userRepository.findById(id);
		user.get().setEnabled(false);
		userRepository.save(user.get());
	}
	
	@Override
	public UserDTO updateUser(Long id, UserDTO userDTO){
		User user = userRepository.findById(id).orElseThrow(() -> new AppException("Not found user with id: " + id, HttpStatus.NOT_FOUND));
		if(userRepository.existsByPhone(userDTO.getPhone()) && !user.getPhone().equals(userDTO.getPhone())) {
			throw new AppException("Phone is already taken!", HttpStatus.BAD_REQUEST);
		}
		user.setUsername(userDTO.getUsername());
		user.setGender(userDTO.isGender());
		user.setBirthday(userDTO.getBirthday());
		user.setPhone(userDTO.getPhone());
		userRepository.save(user);
		return modelMapper.map(user, UserDTO.class);
	}
	
	@Override
	public Long randomCode() {
		//code là ngày thangs năm giờ phút giây
		return Long.parseLong(String.valueOf(System.currentTimeMillis()));
	}
}


