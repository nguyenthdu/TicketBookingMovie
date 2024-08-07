package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.request.SigninRequest;
import com.app.TicketBookingMovie.payload.request.SignupRequest;
import com.app.TicketBookingMovie.payload.response.JwtResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    JwtUtils jwtUtils;
    private final UserService userService;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    AuthenticationManager authenticationManager;

    public UserController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils = jwtUtils;
        this.userService = userService;

    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestParam("email") String email,
            @RequestParam("password") String password) {
        SigninRequest loginRequest = new SigninRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (!userDetails.isEnabled()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getCode(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles,
                    userDetails.getBirthday(),
                    userDetails.getPhone(),
                    userDetails.isEnabled(),
                    userDetails.isGender(),
                    userDetails.getCreatedDate()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email hoặc mật khẩu không đúng!!!",
                    HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Tài khoản này chưa được kích hoạt!",
                    HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }

    }

    // đăng nhập chỉ user có quyển mor hoặc admin mới có thể đăng nhập
    @PostMapping("/signinWeb")
    public ResponseEntity<?> authenticateUserWeb(@Valid @RequestParam("email") String email,
            @RequestParam("password") String password) {
        SigninRequest loginRequest = new SigninRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            if (!userDetails.isEnabled()) {
                return ResponseEntity.badRequest().build();
            }
            if (!roles.contains("ROLE_ADMIN") && !roles.contains("ROLE_MODERATOR")) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Bạn không có quyền truy cập vào trang này!!!",
                                HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
            }
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getCode(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles,
                    userDetails.getBirthday(),
                    userDetails.getPhone(),
                    userDetails.isEnabled(),
                    userDetails.isGender(),
                    userDetails.getCreatedDate()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email hoặc mật khẩu không đúng!!!",
                    HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Tài khoản này chưa được kích hoạt!",
                    HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }

    }

    // TODO: get all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
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

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // TODO: register morderater
    @PostMapping("/mor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> registerMor(@Valid @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("gender") boolean gener,
            @RequestParam("password") String password) {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername(username);
        signUpRequest.setEmail(email);
        signUpRequest.setPhone(phone);
        signUpRequest.setGender(gener);
        signUpRequest.setPassword(password);
        try {
            userService.createMor(signUpRequest);
            return ResponseEntity.ok(
                    new MessageResponse("Tạo nhân viên thành công!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new MessageResponse(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    // TODO: register user
    // @PreAuthorize("hasRole('MODERATOR')")
    @PostMapping()
    public ResponseEntity<MessageResponse> registerUser(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("gender") boolean gender,
            @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
            @RequestParam("phone") String phone,
            @RequestParam("password") String password) {
        SignupRequest signUpRequest = new SignupRequest();
        signUpRequest.setUsername(username);
        signUpRequest.setEmail(email);
        signUpRequest.setGender(gender);
        signUpRequest.setBirthday(birthday);
        signUpRequest.setPhone(phone);
        signUpRequest.setPassword(password);

        try {
            userService.createUser(signUpRequest);
            return ResponseEntity.ok(new MessageResponse(
                    "Đăng ký thành công. Vui lòng vào gmail để xác thực tài khoản sau đó mới có thể đăng nhập.",
                    HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }

    @RequestMapping(value = "/confirm-account", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token") String confirmationToken) {
        return userService.confirmEmail(confirmationToken);
    }

    // update
    @PutMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<MessageResponse> updateUser(@RequestParam("id") Long id,
            @RequestParam("username") String username, @RequestParam("gender") boolean gender,
            @RequestParam("birthday") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
            @RequestParam("phone") String phone) {
        UserDto userDTO = new UserDto();
        userDTO.setUsername(username);
        userDTO.setGender(gender);
        userDTO.setBirthday(birthday);
        userDTO.setPhone(phone);
        try {
            userService.updateUser(id, userDTO);
            return ResponseEntity.ok(new MessageResponse("Cập nhật người dùng thành công với di: " + id,
                    HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new MessageResponse(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    // delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().body(new MessageResponse("Xóa người dùng thành công với id: " + id,
                    HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new MessageResponse(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    // update password
    @PostMapping("/password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<?> updateUserPassword(@RequestParam Long id, @RequestParam String oldPassword,
            @RequestParam String newPassword, @RequestParam String confirmPassword) {
        try {
            userService.updatePassword(id, oldPassword, newPassword, confirmPassword);
            return ResponseEntity.ok().body(
                    new MessageResponse("Đổi mật khẩu thành công!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new MessageResponse(e.getMessage(), e.getStatus(), Instant.now().toString()));
        }
    }

    public void createRoles() {
        userService.createRoles();
    }

    @PostConstruct
    public void createAdmin() {
        createRoles();
        userService.createAdmin();
    }

    // @PostMapping("/userInTicket")
    // public ResponseEntity<MessageResponse> createUserInTicket(
    // @RequestParam("username") String username,
    // @RequestParam("email") String email) {
    // UserDto userDto = new UserDto();
    // userDto.setUsername(username);
    // userDto.setEmail(email);
    // try {
    // userService.createUserInTicket(userDto);
    // return ResponseEntity.ok(new MessageResponse("Đăng ký thành công!",
    // HttpStatus.OK.value(), Instant.now().toString()));
    // } catch (AppException e) {
    // return ResponseEntity.status(e.getStatus()).body(new
    // MessageResponse(e.getMessage(), e.getStatus(), Instant.now().toString()));
    // }
    // }

    @PostMapping("/guest")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<UserDto> createGuest() {
        try {
            return ResponseEntity.ok(userService.createGuest());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

    }
}
