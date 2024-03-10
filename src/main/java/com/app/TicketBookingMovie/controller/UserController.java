package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.dtos.UserDTO;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	JwtUtils jwtUtils;
	@Autowired
	UserRepository userRepository;
	private final UserService userService;
	
	@GetMapping
	public ResponseEntity<PageResponse<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "2") Integer size, @RequestParam(required = false) String phone, @RequestParam(required = false) Long code, @RequestParam(required = false) String email) {
		PageResponse<UserDTO> userPageResponse = new PageResponse<>();
		userPageResponse.setContent(userService.getAllUsers(page, size, phone, code, email));
		userPageResponse.setTotalElements(userRepository.count());
		userPageResponse.setTotalPages((int) Math.ceil((double) userRepository.count() / size));
		userPageResponse.setCurrentPage(page);
		userPageResponse.setPageSize(size);
		return ResponseEntity.ok().body(userPageResponse);
	}
	
	@GetMapping("/code")
	public ResponseEntity<UserDTO> getUserByCode(@RequestParam("code") Long code) {
		UserDTO userDTO = userService.getUserByCode(code);
		return ResponseEntity.ok().body(userDTO);
	}
	
	@GetMapping("/username")
	public ResponseEntity<UserDTO> getUserByUsername(@RequestParam("username") String username) {
		UserDTO userDTO = userService.getUserByUsername(username);
		return ResponseEntity.ok().body(userDTO);
	}
	
	@GetMapping("/email")
	public ResponseEntity<UserDTO> getUserByEmail(@RequestParam("email") String email) {
		UserDTO userDTO = userService.getUserByEmail(email);
		return ResponseEntity.ok().body(userDTO);
	}
	
	@GetMapping("/phone")
	public ResponseEntity<UserDTO> getUserByPhone(@RequestParam("phone") String phone) {
		UserDTO userDTO = userService.getUserByPhone(phone);
		return ResponseEntity.ok().body(userDTO);
	}
	
	//TODO: Lấy thông tin của user đang đăng nhập theo token vào cookie
	@GetMapping("/profile")
	public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
		String jwt = jwtUtils.getJwtFromCookies(request);
		if(jwt != null && jwtUtils.validateJwtToken(jwt)) {
			String email = jwtUtils.getEmailFromJwtToken(jwt);
			User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + email));
			// Tạo một DTO để trả về thông tin user mà không bao gồm mật khẩu
			UserDTO userDTO = new UserDTO();
			userDTO.setId(user.getId());
			userDTO.setCode(user.getCode());
			userDTO.setUsername(user.getUsername());
			userDTO.setEmail(user.getEmail());
			userDTO.setGender(user.isGender());
			userDTO.setBirthday(user.getBirthday());
			userDTO.setPhone(user.getPhone());
			userDTO.setRoles(user.getRoles());
			userDTO.setEnabled(user.isEnabled());
			userDTO.setCreatedDate(user.getCreatedDate());
			return ResponseEntity.ok(userDTO);
		}
		return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Invalid JWT token", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
	}
	
	//TODO: Cập nhật thông tin của user đang đăng nhập theo token vào cookie
	@PutMapping("/profile")
	public ResponseEntity<?> updateUserProfile(@RequestParam("username") String username, @RequestParam("gender") boolean gender, @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday, @RequestParam("phone") String phone, HttpServletRequest request) {
		String jwt = jwtUtils.getJwtFromCookies(request);
		if(jwt != null && jwtUtils.validateJwtToken(jwt)) {
			String currentEmail = jwtUtils.getEmailFromJwtToken(jwt);
			User user = userRepository.findByEmail(currentEmail).orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + currentEmail));
			user.setUsername(username);
			user.setGender(gender);
			user.setBirthday(birthday);
			user.setPhone(phone);
			// Lưu lại thông tin đã cập nhật
			userRepository.save(user);
			// Trả về thông tin user sau khi cập nhật
			UserDTO updatedUserDTO = new UserDTO();
			updatedUserDTO.setId(user.getId());
			updatedUserDTO.setCode(user.getCode());
			updatedUserDTO.setUsername(user.getUsername());
			updatedUserDTO.setEmail(user.getEmail());
			updatedUserDTO.setGender(user.isGender());
			updatedUserDTO.setBirthday(user.getBirthday());
			updatedUserDTO.setPhone(user.getPhone());
			updatedUserDTO.setRoles(user.getRoles());
			updatedUserDTO.setEnabled(user.isEnabled());
			updatedUserDTO.setCreatedDate(user.getCreatedDate());
			return ResponseEntity.ok(updatedUserDTO);
		}
		return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Invalid JWT token", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
	}
	
	//delete
	@DeleteMapping()
	public ResponseEntity<?> deleteUser(@RequestParam("id") Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok().body(new MessageResponseDTO("User deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
	}
	//update user
}
