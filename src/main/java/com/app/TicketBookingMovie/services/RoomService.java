package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.RoomDto;

import java.util.List;

public interface RoomService {
    void createRoom(RoomDto roomDto);

    void updateRoom(RoomDto roomDto);

    RoomDto getRoomById(Long id);
    void deleteRoom(Long id);
    List<RoomDto> getAllRoomsPage(Integer page, Integer size, String code, String name, Long cinemaId);

    long countAllRooms(String code, String name, Long cinemaId);
}
