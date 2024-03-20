package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class CinemaDto {
    private Long id;
    private String code;
    private String name;
    private int totalRoom;
    private AddressDto address;
    private boolean status = true;
    private Set<Long> roomIds;
    private Set<Long> movieIds;
}
