package com.app.TicketBookingMovie.security;


import com.app.TicketBookingMovie.dtos.ErrorDTO;
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
//		logger.error("Unauthorized error: {}", authException.getMessage());
//		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//		final Map<String, Object> body = new HashMap<>();
//		body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
//		body.put("error", "Unauthorized");
//		body.put("message", authException.getMessage());
//		body.put("path", request.getServletPath());
//		final ObjectMapper mapper = new ObjectMapper();
//		mapper.writeValue(response.getOutputStream(), body);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);// Đặt mã trạng thái HTTP của phản hồi thành 401 (Unauthorized) để chỉ ra rằng yêu cầu không được xác thực.
		response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);//Phản hồi daạng json
		OBJECT_MAPPER.writeValue(response.getOutputStream(), new ErrorDTO("Unauthorized path", HttpServletResponse.SC_UNAUTHORIZED, Instant.now().toString()));// Sử dụng một đối tượng ObjectMapper để chuyển đối tượng ErrorDto thành dữ liệu JSON và ghi vào đầu ra của phản hồi.
	}
}
