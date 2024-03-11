package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.app.TicketBookingMovie.dtos.SigninDTO;
import com.app.TicketBookingMovie.dtos.SignupDTO;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.ERole;
import com.app.TicketBookingMovie.models.RefreshToken;
import com.app.TicketBookingMovie.models.Role;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.RoleRepository;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.RefreshTokenService;
import com.app.TicketBookingMovie.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//for Angular Client (withCredentials)
//@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final RefreshTokenService refreshTokenService;
	@Autowired
	AuthenticationManager authenticationManager;
	@Autowired
	UserRepository userRepository;
	@Autowired
	RoleRepository roleRepository;
	@Autowired
	PasswordEncoder encoder;
	@Autowired
	JwtUtils jwtUtils;
	@Autowired
	UserService userService;
	
@PostMapping("/signin")
public ResponseEntity<?> authenticateUser(@Valid @RequestBody SigninDTO signinDTO) {
    try {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinDTO.getEmail(), signinDTO.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()).body(new MessageResponseDTO("User signed in successfully!", HttpStatus.OK.value(), Instant.now().toString()));
    } catch (BadCredentialsException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO("Incorrect email or password!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
    }
}

	
	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDTO signUpDTO) {
		if(userRepository.existsByUsername(signUpDTO.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Username is already taken!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		if(userRepository.existsByEmail(signUpDTO.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Email is already in use!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		if(userRepository.existsByPhone(signUpDTO.getPhone())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Phone is already in use!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		// Create new user's account
		User user = new User(signUpDTO.getUsername(), signUpDTO.getEmail(), signUpDTO.isGender(), signUpDTO.getBirthday(), signUpDTO.getPhone(), encoder.encode(signUpDTO.getPassword()));
		// Set default role as ROLE_USER
		user.setCode(userService.randomCode());
		Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);
		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
	}

	@PostMapping("/signup/admin")
	public ResponseEntity<?> registerAdmin(@Valid @RequestBody SignupDTO signUpDTO) {
		if(userRepository.existsByUsername(signUpDTO.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Username is already taken!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		if(userRepository.existsByEmail(signUpDTO.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Email is already in use!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		if(userRepository.existsByPhone(signUpDTO.getPhone())) {
			return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Phone is already in use!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
		}
		// Create new user's account
		User user = new User(signUpDTO.getUsername(), signUpDTO.getEmail(), signUpDTO.isGender(), signUpDTO.getBirthday(), signUpDTO.getPhone(), encoder.encode(signUpDTO.getPassword()));
		// Set default role as ROLE_USER
		user.setCode(userService.randomCode());
		Role userRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);
		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
	}
	//
//		@PostMapping("/signup")
//		public ResponseEntity<?> registerUser(@Valid @RequestBody SignupDTO signUpDTO) {
//			if(userRepository.existsByUsername(signUpDTO.getUsername())) {
//				return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Username is already taken!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
//			}
//			if(userRepository.existsByEmail(signUpDTO.getEmail())) {
//				return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Email is already in use!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
//			}
//			// Create new user's account
//			User user = new User( signUpDTO.getUsername(), signUpDTO.getEmail(), signUpDTO.isGender(), signUpDTO.getBirthday(), signUpDTO.getPhone(), encoder.encode(signUpDTO.getPassword()));
//			Long code = userService.randomCode();
//			user.setCode(code);
//
//			Set<String> strRoles = signUpDTO.getRoles();
//			Set<Role> roles = new HashSet<>();
//			if(strRoles == null) {
//				Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//				roles.add(userRole);
//			} else {
//				strRoles.forEach(role -> {
//					switch (role) {
//						case "admin":
//							Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//							roles.add(adminRole);
//							break;
//						case "mod":
//							Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//							roles.add(modRole);
//							break;
//						default:
//							Role userRole = roleRepository.findByName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//							roles.add(userRole);
//					}
//				});
//			}
//			user.setRoles(roles);
//			userRepository.save(user);
//			return ResponseEntity.ok(new MessageResponseDTO("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
//		}

	
	@PostMapping("/signout")
	public ResponseEntity<?> logoutUser() {
		Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if(principle.toString() != "anonymousUser") {
			Long userId = ((UserDetailsImpl) principle).getId();
			refreshTokenService.deleteByUserId(userId);
		}
		ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
		ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()).body(new MessageResponseDTO("You've been signed out!", HttpStatus.OK.value(), Instant.now().toString()));
	}
	
	@PostMapping("/refreshtoken")
	public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
		String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
		if((refreshToken != null) && (refreshToken.length() > 0)) {
			return refreshTokenService.findByToken(refreshToken).map(refreshTokenService::verifyExpiration).map(RefreshToken::getUser).map(user -> {
				ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
				return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new MessageResponseDTO("Token is refreshed successfully!", HttpStatus.OK.value(), Instant.now().toString()));
			}).orElseThrow(() -> new AppException("Refresh token is not in database!", HttpStatus.BAD_REQUEST));
		}
		return ResponseEntity.badRequest().body(new MessageResponseDTO("Refresh token is not in cookies!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
	}
}
