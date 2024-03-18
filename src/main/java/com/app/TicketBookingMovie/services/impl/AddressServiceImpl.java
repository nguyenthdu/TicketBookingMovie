package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Address;
import com.app.TicketBookingMovie.repository.AddressRepository;
import com.app.TicketBookingMovie.services.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
    private final ModelMapper modelMapper;
    private final AddressRepository addressRepository;

    @Autowired
    public AddressServiceImpl(ModelMapper modelMapper, AddressRepository addressRepository) {
        this.modelMapper = modelMapper;
        this.addressRepository = addressRepository;
    }

    @Override
    public AddressDto createAddress(AddressDto addressDto) {
        Address address = modelMapper.map(addressDto, Address.class);
        addressRepository.save(address);
        return modelMapper.map(address, AddressDto.class);
    }

    @Override
    public AddressDto getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException("Address not found with id: " + addressId, HttpStatus.NOT_FOUND));
        return modelMapper.map(address, AddressDto.class);
    }

    @Override
    public AddressDto updateAddress(AddressDto addressDto) {
        Address address = addressRepository.findById(addressDto.getId())
                .orElseThrow(() -> new AppException("Address not found with id: " + addressDto.getId(), HttpStatus.NOT_FOUND));
        address.setStreet(addressDto.getStreet());
        address.setDistrict(addressDto.getDistrict());
        address.setCity(addressDto.getCity());
        address.setNation(addressDto.getNation());
        addressRepository.save(address);
        return modelMapper.map(address, AddressDto.class);
    }
}
