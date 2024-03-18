package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.AddressDto;

public interface AddressService {
    AddressDto createAddress(AddressDto addressDto);
    AddressDto getAddressById(Long addressId);
    AddressDto updateAddress( AddressDto addressDto);
}
