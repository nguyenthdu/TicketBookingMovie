package com.app.TicketBookingMovie.dtos;

import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private String street;
    private String district;
    private String city;
    private String nation;

}
