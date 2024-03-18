package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.dtos.CinemaDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Address;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.repository.AddressRepository;
import com.app.TicketBookingMovie.repository.CinemaRepository;
import com.app.TicketBookingMovie.services.AddressService;
import com.app.TicketBookingMovie.services.CinemaService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class CinemaServiceImpl implements CinemaService {
    private final ModelMapper modelMapper;
    private final CinemaRepository cinemaRepository;
    private final AddressService addressService;
    private final AddressRepository addressRepository;

    @Autowired
    public CinemaServiceImpl(ModelMapper modelMapper, CinemaRepository cinemaRepository, AddressService addressService, AddressRepository addressRepository) {
        this.modelMapper = modelMapper;
        this.cinemaRepository = cinemaRepository;
        this.addressService = addressService;
        this.addressRepository = addressRepository;
    }

    @Override
    public CinemaDto createCinema(CinemaDto cinemaDto) {
        if (cinemaRepository.findByName(cinemaDto.getName()).isPresent()) {
            throw new AppException("name: " + cinemaDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        Cinema cinema = new Cinema();
        cinema.setCode(randomCode());
        cinema.setName(cinemaDto.getName());
        AddressDto addressDto = cinemaDto.getAddress();
        addressDto = addressService.createAddress(addressDto); // Save the address and get the saved object
        Address address = modelMapper.map(addressDto, Address.class); // Map the saved AddressDto to Address
        cinema.setAddress(address);
        cinema.setTotalRoom(cinemaDto.getTotalRoom());
        cinema.setStatus(cinemaDto.isStatus());
        cinemaRepository.save(cinema);
        return modelMapper.map(cinema, CinemaDto.class);
    }

    public String randomCode() {
        Random random = new Random();
        String code;
        int number = random.nextInt(1000);
        code = "RAP" + System.currentTimeMillis() + number;
        return code;
    }

    @Override
    public CinemaDto getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(
                () -> new AppException("Cinema not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(cinema, CinemaDto.class);

    }

    @Override
    public CinemaDto updateCinema(CinemaDto cinemaDto) {
        Cinema cinema = cinemaRepository.findById(cinemaDto.getId()).orElseThrow(
                () -> new AppException("Cinema not found with id: " + cinemaDto.getId(), HttpStatus.NOT_FOUND));
        cinema.setName(cinemaDto.getName());
        //handle address
        Address address = cinema.getAddress();
        AddressDto addressDto = cinemaDto.getAddress();
        addressDto.setId(address.getId());
        addressService.updateAddress(addressDto);

        cinema.setTotalRoom(cinemaDto.getTotalRoom());
        cinema.setStatus(cinemaDto.isStatus());
        cinemaRepository.save(cinema);
        return modelMapper.map(cinema, CinemaDto.class);
    }

    @Override
    public void deleteCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(
                () -> new AppException("Cinema not found with id: " + id, HttpStatus.NOT_FOUND));
        cinemaRepository.delete(cinema);

    }

    @Override
    public List<CinemaDto> getAllCinemas(int page, int size, String code, String name, String street, String district, String city, String nation) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cinema> cinemaPage;
        if (code != null && !code.isEmpty()) {
            cinemaPage = cinemaRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            cinemaPage = cinemaRepository.findByNameContaining(name, pageable);
        } else if (street != null && !street.isEmpty()) {
            cinemaPage = cinemaRepository.findByAddressStreet(street, pageable);
        } else if (district != null && !district.isEmpty()) {
            cinemaPage = cinemaRepository.findByAddressDistrict(district, pageable);
        } else if (city != null && !city.isEmpty()) {
            cinemaPage = cinemaRepository.findByAddressCity(city, pageable);
        } else if (nation != null && !nation.isEmpty()) {
            cinemaPage = cinemaRepository.findByAddressNation(nation, pageable);
        } else {
            cinemaPage = cinemaRepository.findAll(pageable);
        }

        return cinemaPage.map(cinema -> modelMapper.map(cinema, CinemaDto.class)).getContent();
    }
}
