package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.ResponseSignin;
import com.app.TicketBookingMovie.dtos.SigninDto;
import com.app.TicketBookingMovie.dtos.UserDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.RefreshToken;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.security.UserDetailsImpl;
import com.app.TicketBookingMovie.services.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

//for Angular Client (withCredentials)
//@CrossOrigin(origins = "http://localhost:8081", maxAge = 3600, allowCredentials="true")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;


    public AuthController(RefreshTokenService refreshTokenService, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.refreshTokenService = refreshTokenService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestParam("email") String email,
                                              @RequestParam("password") String password) {
        SigninDto signinDTO = new SigninDto();
        signinDTO.setEmail(email);
        signinDTO.setPassword(password);

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(signinDTO.getEmail(), signinDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // Kiểm tra trạng thái của người dùng
            if (!userDetails.isEnabled()) {
                return ResponseEntity.badRequest().build();
            }

            ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);
            List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority()).collect(Collectors.toList());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
            ResponseCookie jwtRefreshCookie = jwtUtils.generateRefreshJwtCookie(refreshToken.getToken());
            ResponseSignin responseSignin= new ResponseSignin();
            //lấy thông tin user
            UserDto userDto = new UserDto();
            userDto.setId(userDetails.getId());
            userDto.setUsername(userDetails.getUsername());
            userDto.setEmail(userDetails.getEmail());
            responseSignin.setAccess_token(jwtCookie.getValue());
            responseSignin.setUser(userDto);
//            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
//                    //trả về token
//                    .body(new MessageResponseDto("User logged in successfully!", HttpStatus.OK.value(), Instant.now().toString()));
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                    //trả về token
                    .body(responseSignin);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Incorrect email or password!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        } catch (DisabledException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto("Account is not activated yet! Please check your email to activate your account!"
                    , HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }


    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        Object principle = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principle.toString() != "anonymousUser") {
            Long userId = ((UserDetailsImpl) principle).getId();
            refreshTokenService.deleteByUserId(userId);
        }
        ResponseCookie jwtCookie = jwtUtils.getCleanJwtCookie();
        ResponseCookie jwtRefreshCookie = jwtUtils.getCleanJwtRefreshCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString()).body(new MessageResponseDto("You've been signed out!", HttpStatus.OK.value(), Instant.now().toString()));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) {
        String refreshToken = jwtUtils.getJwtRefreshFromCookies(request);
        if ((refreshToken != null) && (refreshToken.length() > 0)) {
            return refreshTokenService.findByToken(refreshToken).map(refreshTokenService::verifyExpiration).map(RefreshToken::getUser).map(user -> {
                ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(user);
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(new MessageResponseDto("Token is refreshed successfully!", HttpStatus.OK.value(), Instant.now().toString()));
            }).orElseThrow(() -> new AppException("Refresh token is not in database!", HttpStatus.BAD_REQUEST));
        }
        return ResponseEntity.badRequest().body(new MessageResponseDto("Refresh token is not in cookies!", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
    }


}
