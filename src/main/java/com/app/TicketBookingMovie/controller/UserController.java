package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.SignupDto;
import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.repository.UserRepository;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/users")
public class UserController {
    JwtUtils jwtUtils;
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(JwtUtils jwtUtils, UserService userService, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.userRepository = userRepository;

    }

    //TODO: get all users
    @GetMapping
    public ResponseEntity<PageResponse<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0", name = "page", required = false) Integer page,
            @RequestParam(defaultValue = "10", name = "size", required = false) Integer size,
            @RequestParam(required = false, name = "code") String code,
            @RequestParam(required = false, name = "username") String username,
            @RequestParam(required = false, name = "phone") String phone,
            @RequestParam(required = false, name = "email") String email,
            @RequestParam(required = false, name = "roleId") Long roleId) {
        PageResponse<UserDto> userPageResponse = new PageResponse<>();
        userPageResponse.setContent(userService.getAllUsersPage(page, size, code, username, phone, email, roleId));
        userPageResponse.setTotalElements(userService.countUsers(code, username, phone, email, roleId));
        userPageResponse.setTotalPages((int) Math.ceil((double) userPageResponse.getTotalElements() / size));
        userPageResponse.setCurrentPage(page);
        userPageResponse.setPageSize(size);
        return ResponseEntity.ok(userPageResponse);
    }


    // TODO: Lấy thông tin của user đang đăng nhập theo token vào cookie
    @GetMapping("/profile")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String email = jwtUtils.getEmailFromJwtToken(jwt);
            User user = userService.getCurrentUser(email);
            return ResponseEntity.ok(user);
        }
        throw new UsernameNotFoundException("User not found with email: " + jwtUtils.getEmailFromJwtToken(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    //TODO: register morderater
    @PostMapping("/mor")
    public ResponseEntity<MessageResponseDto> registerMor(@Valid
                                                          @RequestParam("username") String username,
                                                          @RequestParam("email") String email,
                                                          @RequestParam("gender") boolean gener,
                                                          @RequestParam("password") String password) {
        SignupDto signUpDTO = new SignupDto();
        signUpDTO.setUsername(username);
        signUpDTO.setEmail(email);
        signUpDTO.setGender(gener);
        signUpDTO.setPassword(password);
        try {
            userService.createMor(signUpDTO);
            return ResponseEntity.ok(new MessageResponseDto("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    //TODO: register user
//    @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping()
    public ResponseEntity<MessageResponseDto> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("gender") boolean gender,
            @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password) {
        SignupDto signUpDTO = new SignupDto();
        signUpDTO.setUsername(username);
        signUpDTO.setEmail(email);
        signUpDTO.setGender(gender);
        signUpDTO.setBirthday(birthday);
        signUpDTO.setPhone(phone);
        signUpDTO.setPassword(password);

        try {
            userService.createUser(signUpDTO);
            return ResponseEntity.ok(new MessageResponseDto("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }

    }

    // TODO: Cập nhật thông tin của user đang đăng nhập theo token vào cookie
    @PutMapping("/profile")
    public ResponseEntity<MessageResponseDto> updateUserProfile(@RequestParam("username") String username, @RequestParam("gender") boolean gender, @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday, @RequestParam("phone") String phone, HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String currentEmail = jwtUtils.getEmailFromJwtToken(jwt);
            UserDto user = new UserDto();
            user.setUsername(username);
            user.setGender(gender);
            user.setBirthday(birthday);
            user.setPhone(phone);
            try {
                userService.updateUserProfile(currentEmail, user);
                return ResponseEntity.ok(new MessageResponseDto("User updated successfully with email: " + currentEmail, HttpStatus.OK.value(), Instant.now().toString()));
            } catch (AppException e) {
                return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponseDto("Error: Invalid JWT token", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
    }

    // update
    @PutMapping()
    public ResponseEntity<MessageResponseDto> updateUser(@RequestParam("id") Long id, @RequestParam("username") String username, @RequestParam("gender") boolean gender, @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday, @RequestParam("phone") String phone) {
        UserDto userDTO = new UserDto();
        userDTO.setUsername(username);
        userDTO.setGender(gender);
        userDTO.setBirthday(birthday);
        userDTO.setPhone(phone);
        try {
            userService.updateUser(id, userDTO);
            return ResponseEntity.ok(new MessageResponseDto("User updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().body(new MessageResponseDto("User deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    // update password
//    @PutMapping("/{id}/password")
//    public ResponseEntity<?> updateUserPassword(@PathVariable Long id, @RequestParam String oldPassword, @RequestParam String newPassword, @RequestParam String confirmPassword) {
//        try {
//            User user = userRepository.findById(id).orElseThrow(() -> new AppException("User not found with id: " + id, HttpStatus.NOT_FOUND));
//            if (!encoder.matches(oldPassword, user.getPassword())) {
//                return ResponseEntity.badRequest().body(new MessageResponseDto("Old password is incorrect!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
//            }
//            if (!newPassword.equals(confirmPassword)) {
//                return ResponseEntity.badRequest().body(new MessageResponseDto("New password and confirm password do not match!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
//            }
//            user.setPassword(encoder.encode(newPassword));
//            userRepository.save(user);
//            return ResponseEntity.ok(new MessageResponseDto("Password updated successfully!", HttpStatus.OK.value(), Instant.now().toString()));
//        } catch (AppException e) {
//            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
//        }
//    }
    public void createRoles() {
        userService.createRoles();
    }

    @PostConstruct
    public void createAdmin() {
        createRoles();
        userService.createAdmin();
    }

    @PostMapping("/userInTicket")
    public ResponseEntity<MessageResponseDto> createUserInTicket(
            @RequestParam("username") String username,
            @RequestParam("email") String email) {
        UserDto userDto = new UserDto();
        userDto.setUsername(username);
        userDto.setEmail(email);
        try {
            userService.createUserInTicket(userDto);
            return ResponseEntity.ok(new MessageResponseDto("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    @PostMapping("/guest")
    public ResponseEntity<MessageResponseDto> createGuest() {
        userService.createGuest();
        return ResponseEntity.ok(new MessageResponseDto("User registered successfully!", HttpStatus.OK.value(), Instant.now().toString()));

    }
}
