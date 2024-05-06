package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class ResponseSignin {
    private String access_token;
    private UserDto user;
}
