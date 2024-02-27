package com.app.TicketBookingMovie.dtos;

//TODO: Chứa message lỗi
public record ErrorDTO(String message, int status, String timestamp) {
}
