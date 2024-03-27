package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.CinemaDto;

import java.util.List;

public interface CinemaService {
    void createCinema(CinemaDto cinemaDto);
    CinemaDto getCinemaById(Long id);
    void updateCinema(CinemaDto cinemaDto);
    void deleteCinemaById(Long id);
    List<CinemaDto> getAllCinemas(int page, int size,String  code, String name, String street,String ward, String district, String city, String nation);
    void countTotalRooms(Long id, int number);

    long countAllCinemas(String code, String name, String street,String ward, String district, String city, String nation);
}
