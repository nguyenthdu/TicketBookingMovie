package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.AddressDto;
import com.app.TicketBookingMovie.dtos.CinemaDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Address;
import com.app.TicketBookingMovie.models.Cinema;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CinemaServiceImpl implements CinemaService {
    private final ModelMapper modelMapper;
    private final CinemaRepository cinemaRepository;
    private final AddressService addressService;

    @Autowired
    public CinemaServiceImpl(ModelMapper modelMapper, CinemaRepository cinemaRepository, AddressService addressService) {
        this.modelMapper = modelMapper;
        this.cinemaRepository = cinemaRepository;
        this.addressService = addressService;

    }

    @Override
    public void createCinema(CinemaDto cinemaDto) {
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
        cinema.setStatus(cinemaDto.isStatus());
        cinema.setTotalRoom(0);
        cinema.setCreatedDate(LocalDateTime.now());
        cinemaRepository.save(cinema);
    }

    public String randomCode() {
        return "RA" + LocalDateTime.now().getNano();
    }

    @Override
    public CinemaDto getCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(
                () -> new AppException("Cinema not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(cinema, CinemaDto.class);

    }

    @Override
    public void updateCinema(CinemaDto cinemaDto) {
        Cinema cinema = cinemaRepository.findById(cinemaDto.getId()).orElseThrow(
                () -> new AppException("Cinema not found with id: " + cinemaDto.getId(), HttpStatus.NOT_FOUND));
        if(!cinemaDto.getName().isEmpty() && !cinemaDto.getName().isBlank()) {
            cinema.setName(cinemaDto.getName());
        } else{
            cinema.setName(cinema.getName());
        }
        //handle address
        Address address = cinema.getAddress();
        AddressDto addressDto = cinemaDto.getAddress();
        addressDto.setId(address.getId());
        addressService.updateAddress(addressDto);
        if(cinemaDto.isStatus() != cinema.isStatus()) {
            cinema.setStatus(cinemaDto.isStatus());
        }else {
            cinema.setStatus(cinema.isStatus());
        }
        cinemaRepository.save(cinema);

    }

    @Override
    public void deleteCinemaById(Long id) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(
                () -> new AppException("Cinema not found with id: " + id, HttpStatus.NOT_FOUND));
        cinemaRepository.delete(cinema);

    }

    @Override
    public List<CinemaDto> getAllCinemas(int page, int size, String code, String name, String street,String ward, String district, String city, String nation) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cinema> cinemaPage;
        if (code != null && !code.isEmpty()) {
            cinemaPage = cinemaRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            cinemaPage = cinemaRepository.findByNameContaining(name, pageable);
        }


        else if (street != null && !street.isEmpty()) {
            cinemaPage = cinemaRepository.findByAddressStreet(street, pageable);
        }

        else if(ward != null && !ward.isEmpty()){
            cinemaPage = cinemaRepository.findByAddressWard(ward, pageable);
        }
        else if (district != null && !district.isEmpty()) {
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

    @Override
    public void countTotalRooms(Long id, int number) {
        Cinema cinema = cinemaRepository.findById(id).orElseThrow(
                () -> new AppException("Cinema not found with id: " + id, HttpStatus.NOT_FOUND));
        cinema.setTotalRoom(cinema.getTotalRoom() + number);
        cinemaRepository.save(cinema);
    }

    @Override
    public long countAllCinemas(String code, String name, String street, String ward ,String district, String city, String nation) {
        if (code != null && !code.isEmpty()) {
            return cinemaRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return cinemaRepository.countByNameContaining(name);
        } else if (street != null && !street.isEmpty()) {
            return cinemaRepository.countByAddressStreet(street);
        }
        else if(ward != null && !ward.isEmpty()){
            return cinemaRepository.countByAddressWard(ward);
        }

        else if (district != null && !district.isEmpty()) {
            return cinemaRepository.countByAddressDistrict(district);
        } else if (city != null && !city.isEmpty()) {
            return cinemaRepository.countByAddressCity(city);
        } else if (nation != null && !nation.isEmpty()) {
            return cinemaRepository.countByAddressNation(nation);
        } else {
            return cinemaRepository.count();
        }
    }


}
