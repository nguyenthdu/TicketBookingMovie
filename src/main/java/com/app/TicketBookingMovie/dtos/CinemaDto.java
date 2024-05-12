package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CinemaDto   {
    private Long id;
    private String code;
    private String name;
    private int totalRoom;
    private AddressDto address;
    private boolean status = true;
//    private Set<Long> roomIds;
//    private Set<Long> movieIds;
    private LocalDateTime createdDate;
}
