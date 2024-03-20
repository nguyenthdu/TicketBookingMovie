package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.SeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Seat;
import com.app.TicketBookingMovie.models.TypeSeat;
import com.app.TicketBookingMovie.repository.RoomRepository;
import com.app.TicketBookingMovie.repository.SeatRepository;
import com.app.TicketBookingMovie.repository.TypeSeatRepository;
import com.app.TicketBookingMovie.services.SeatService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class SeatServiceImpl implements SeatService {
    private final ModelMapper modelMapper;
    private final SeatRepository seatRepository;
    private final TypeSeatRepository typeSeatRepository;
    private final RoomRepository roomRepository;

    @Autowired
    public SeatServiceImpl(ModelMapper modelMapper, SeatRepository seatRepository, TypeSeatRepository typeSeatRepository, RoomRepository roomRepository) {
        this.modelMapper = modelMapper;
        this.seatRepository = seatRepository;
        this.typeSeatRepository = typeSeatRepository;
        this.roomRepository = roomRepository;
    }

    public String randomCode() {
        Random random = new Random();
        String code;
        code = "GHE" + System.currentTimeMillis();
        return code;
    }

    public String handleNameSeat(int row, int col) {
        char rowChar = (char) ('A' + row - 1); // Convert row number to character starting from 'A'
        return rowChar + String.valueOf(col); // Combine row character and column number to form seat name
    }

    @Override
    public SeatDto createSeat(SeatDto seatDto) {
        Seat seat = modelMapper.map(seatDto, Seat.class);
        seat.setCode(randomCode());
        seat.setName(handleNameSeat(seatDto.getSeatRow(), seat.getSeatColumn()));
        TypeSeat typeSeat = typeSeatRepository.findById(seatDto.getSeatTypeId())
                .orElseThrow(() -> new AppException("Seat type not found" + seatDto.getSeatTypeId(), HttpStatus.NOT_FOUND));
        seat.setSeatType(typeSeat);
        seatRepository.save(seat);
        return modelMapper.map(seat, SeatDto.class);
    }

    @Override
    public SeatDto getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new AppException("Seat not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(seat, SeatDto.class);
    }


    @Override
    public void deleteSeatById(Long id) {
        seatRepository.deleteById(id);
    }


}
