package com.app.TicketBookingMovie.security;

import com.app.TicketBookingMovie.services.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
// (securedEnabled = true,
// jsr250Enabled = true,
// prePostEnabled = true) // by default
public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter {
	@Autowired
	UserServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;
	private final PasswordConfig passwordConfig;

	public WebSecurityConfig(PasswordConfig passwordConfig) {
		this.passwordConfig = passwordConfig;
	}

	@Bean
	public AuthTokenFilter authenticationJwtTokenFilter() {
		return new AuthTokenFilter();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordConfig.passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/test/**", "/api/users/**", "/api/genre/**", "/api/movie/**",
								"/api/address/**", "/api/cinema/**", "/api/seat/**", "api/categoryFood/**",
								"/api/food/**",
								"/api/typeSeat/**", "api/room/**", "/api/showtime/**", "/api/ticket/**",
								"/api/promotion/**",
								"/api/price/**", "/api/invoice/**", "/api/aws/**", "/api/statistical/**")
						.permitAll()
						.anyRequest().authenticated())
				.authenticationProvider(authenticationProvider());

		http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();

	}
}
