package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.UserDTO;
import com.app.TicketBookingMovie.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class AdminController {
	private final UserService userService;
	

	
	@GetMapping("/users")
	public List<UserDTO> getAllUsers() {
		return userService.getAllUsers();
	}
	@GetMapping("/users/code")
	public ResponseEntity<UserDTO> getUserByCode(@RequestParam("code") Integer code) {
		UserDTO userDTO = userService.getUserByCode(code);
		return ResponseEntity.ok().body(userDTO);
	}
	@GetMapping("/users/username")
	public ResponseEntity<UserDTO> getUserByUsername(@RequestParam("username") String username) {
		UserDTO userDTO = userService.getUserByUsername(username);
		return ResponseEntity.ok().body(userDTO);
	}
	@GetMapping("/users/email")
	public ResponseEntity<UserDTO> getUserByEmail(@RequestParam("email") String email) {
		UserDTO userDTO = userService.getUserByEmail(email);
		return ResponseEntity.ok().body(userDTO);
	}
	@GetMapping("/users/phone")
	public ResponseEntity<UserDTO> getUserByPhone(@RequestParam("phone") String phone) {
		UserDTO userDTO = userService.getUserByPhone(phone);
		return ResponseEntity.ok().body(userDTO);
	}
	
}
