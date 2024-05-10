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
                .orElseThrow(() -> new AppException("Không tìm thấy địa chỉ với id: " + addressId, HttpStatus.NOT_FOUND));
        return modelMapper.map(address, AddressDto.class);
    }

    @Override
    public AddressDto updateAddress(AddressDto addressDto) {
        Address address = addressRepository.findById(addressDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy địa chỉ với id: " + addressDto.getId(), HttpStatus.NOT_FOUND));
        if(!addressDto.getWard().isEmpty() && !addressDto.getWard().isBlank()){
            address.setWard(addressDto.getWard());
        } else {
            address.setWard(address.getWard());
        }
        if (!addressDto.getStreet().isEmpty() && !addressDto.getStreet().isBlank()) {
            address.setStreet(addressDto.getStreet());
        } else {
            address.setStreet(address.getStreet());
        }
        if (!addressDto.getDistrict().isEmpty() && !addressDto.getDistrict().isBlank()) {
            address.setDistrict(addressDto.getDistrict());
        } else {
            address.setDistrict(address.getDistrict());
        }
        if (!addressDto.getCity().isEmpty() && !addressDto.getCity().isBlank()) {
            address.setCity(addressDto.getCity());
        } else {
            address.setCity(address.getCity());
        }
        if (!addressDto.getNation().isEmpty() && !addressDto.getNation().isBlank()) {
            address.setNation(addressDto.getNation());
        } else {
            address.setNation(address.getNation());
        }
        addressRepository.save(address);
        return modelMapper.map(address, AddressDto.class);
    }
}
