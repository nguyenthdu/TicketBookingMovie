package com.app.TicketBookingMovie.exception;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
	@ExceptionHandler(value = AppException.class)
	@ResponseBody
	public ResponseEntity<MessageResponseDto> handleException(AppException ex) {
		return ResponseEntity.status(ex.getStatus()).body(new MessageResponseDto(ex.getMessage(), ex.getStatus(), ex.getTimestamp()));//chứa thông tin phản hồi
	}
}