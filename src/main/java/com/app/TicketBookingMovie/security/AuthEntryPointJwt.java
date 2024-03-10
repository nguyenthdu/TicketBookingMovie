package com.app.TicketBookingMovie.security;


import com.app.TicketBookingMovie.dtos.MessageResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;


@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	@Override
	public void commence(
			HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// Đặt mã trạng thái HTTP của phản hồi thành 401 (Unauthorized) để chỉ ra rằng yêu cầu không được xác thực.
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);//Phản hồi daạng json
		OBJECT_MAPPER.writeValue(response.getOutputStream(), new MessageResponseDTO("Unauthorized path", HttpServletResponse.SC_UNAUTHORIZED, Instant.now().toString()));// Sử dụng một đối tượng ObjectMapper để chuyển đối tượng ErrorDto thành dữ liệu JSON và ghi vào đầu ra của phản hồi.
	}
}
