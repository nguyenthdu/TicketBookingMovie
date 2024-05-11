package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.AddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@RestController
@RequestMapping("api/address")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createAddress(
            @RequestParam("street") String street,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(street);
        addressDto.setDistrict(district);
        addressDto.setCity(city);
        addressDto.setNation(nation);
        try {
            addressService.createAddress(addressDto);
            return ResponseEntity.ok(new MessageResponse("Tạo địa chỉ thành công." + addressDto.getId(), HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<AddressDto> getAddressById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressDto> updateAddressById(
            @RequestParam("id") Long id,
            @RequestParam("street") String street,
            @RequestParam("district") String district,
            @RequestParam("city") String city,
            @RequestParam("nation") String nation) {
        AddressDto addressDto = new AddressDto();
        addressDto.setId(id);
        addressDto.setStreet(street);
        addressDto.setDistrict(district);
        addressDto.setCity(city);
        addressDto.setNation(nation);
        return ResponseEntity.ok(addressService.updateAddress(addressDto));
    }


}
