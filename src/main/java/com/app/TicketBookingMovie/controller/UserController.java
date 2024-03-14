package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.dtos.UserDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
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
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	private final UserService userService;
	
	@GetMapping
	public ResponseEntity<PageResponse<UserDTO>> getAllUsers(@RequestParam(defaultValue = "0", name = "page") Integer page, @RequestParam(defaultValue = "2", name = "size") Integer size, @RequestParam(required = false, name = "code") Long code, @RequestParam(required = false, name = "username") String username, @RequestParam(required = false, name = "phone") String phone, @RequestParam(required = false, name = "email") String email) {
		PageResponse<UserDTO> userPageResponse = new PageResponse<>();
		userPageResponse.setContent(userService.getAllUsers(page, size, code, username, phone , email));
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
	
	// TODO: Lấy thông tin của user đang đăng nhập theo token vào cookie
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
	
	// TODO: Cập nhật thông tin của user đang đăng nhập theo token vào cookie
	@PutMapping()
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
			return ResponseEntity.ok(new MessageResponseDTO("User updated successfully with email: " + currentEmail, HttpStatus.OK.value(), Instant.now().toString()));
		}
		return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Invalid JWT token", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
	}
	
	// update
	@PutMapping("/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
		try {
			userService.updateUser(id, userDTO);
			return ResponseEntity.ok(new MessageResponseDTO("User updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
		} catch (AppException e) {
			return ResponseEntity.status(e.getStatus()).body(new MessageResponseDTO(e.getMessage(), e.getStatus(), Instant.now().toString()));
		}
	}
	
	// delete
	@DeleteMapping()
	public ResponseEntity<?> deleteUser(@RequestParam("id") Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok().body(new MessageResponseDTO("User deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
	}
	
	// update password
	@PutMapping("/{id}/password")
	public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String confirmPassword) {
		try {
			User user = userRepository.findById(id).orElseThrow(() -> new AppException("User not found with id: " + id, HttpStatus.NOT_FOUND));
			if(!encoder.matches(oldPassword, user.getPassword())) {
				return ResponseEntity.badRequest().body(new MessageResponseDTO("Old password is incorrect!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
			}
			if(!newPassword.equals(confirmPassword)) {
				return ResponseEntity.badRequest().body(new MessageResponseDTO("New password and confirm password do not match!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
			}
			user.setPassword(encoder.encode(newPassword));
			userRepository.save(user);
			return ResponseEntity.ok(new MessageResponseDTO("Password updated successfully!", HttpStatus.OK.value(), Instant.now().toString()));
		} catch (AppException e) {
			return ResponseEntity.status(e.getStatus()).body(new MessageResponseDTO(e.getMessage(), e.getStatus(), Instant.now().toString()));
		}
	}
}
