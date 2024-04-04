package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.RoomDto;
import com.app.TicketBookingMovie.dtos.SeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.models.Room;
import com.app.TicketBookingMovie.models.Seat;
import com.app.TicketBookingMovie.models.enums.ETypeRoom;
import com.app.TicketBookingMovie.repository.CinemaRepository;
import com.app.TicketBookingMovie.repository.RoomRepository;
import com.app.TicketBookingMovie.services.CinemaService;
import com.app.TicketBookingMovie.services.RoomService;
import com.app.TicketBookingMovie.services.SeatService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoomServiceImpl implements RoomService {
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final SeatService seatService;
    private final CinemaRepository cinemaRepository;
    private final CinemaService cinemaService;

    @Autowired
    public RoomServiceImpl(ModelMapper modelMapper, RoomRepository roomRepository, SeatService seatService, CinemaRepository cinemaRepository, CinemaService cinemaService) {
        this.modelMapper = modelMapper;
        this.roomRepository = roomRepository;
        this.seatService = seatService;
        this.cinemaRepository = cinemaRepository;
        this.cinemaService = cinemaService;

    }

    @Override
    public void createRoom(RoomDto roomDto) {
        // Map RoomDto to Room
        Room room = modelMapper.map(roomDto, Room.class);

        room.setCode(randomCode());
        Cinema cinema = cinemaRepository.findById(roomDto.getCinemaId())
                .orElseThrow(() -> new AppException("Cinema not found with id:" + roomDto.getCinemaId(), HttpStatus.NOT_FOUND));
        //nếu tên rạp trong cinema đã tồn tại thì thông báo lỗi
        if (roomRepository.findByName(roomDto.getName()).isPresent()) {
            throw new AppException("name: " + roomDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        room.setCinema(cinema);
        //handle type
        if (roomDto.getType().equals("2D")) {
            room.setType(ETypeRoom.ROOM2D);
        } else if (roomDto.getType().equals("3D")) {
            room.setType(ETypeRoom.ROOM3D);
        } else {
            room.setType(ETypeRoom.ROOM4D);
        }
        // Create seats and add them to the room
        Set<Seat> seats = new HashSet<>();
        for (SeatDto seatDto : roomDto.getSeats()) {
            SeatDto seat = seatService.createSeat(seatDto);
            seats.add(modelMapper.map(seat, Seat.class));
        }
        if(roomDto.getPrice()<0){
            throw new AppException("Price must be greater than 0", HttpStatus.BAD_REQUEST);
        }else{
            room.setPrice(roomDto.getPrice());
        }
        room.setSeats(seats);
        room.setTotalSeats(room.getSeats().size());
        room.setCreatedDate(LocalDateTime.now());
        // Save the room
        roomRepository.save(room);
        cinemaService.countTotalRooms(roomDto.getCinemaId(), 1);
        // Create seats and add them to the room
        modelMapper.map(room, RoomDto.class);
    }

    public String randomCode() {
       return "PH"+ LocalDateTime.now().getNano();

    }

    @Override
    public void updateRoom(RoomDto roomDto) {
        // Retrieve the existing Room
        Room room = roomRepository.findById(roomDto.getId())
                .orElseThrow(() -> new AppException("Room not found with id:" + roomDto.getId(), HttpStatus.NOT_FOUND));
        //nếu tên rạp trong cinema đã tồn tại thì thông báo lỗi
        if (roomRepository.findByName(roomDto.getName()).isPresent()) {
            throw new AppException("name: " + roomDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        // Create a set to hold the seats to be removed
        Set<Seat> seatsToRemove = new HashSet<>(room.getSeats());

        // Add new seats to the room or keep existing ones
        for (SeatDto seatDto : roomDto.getSeats()) {
            Seat existingSeat = room.getSeats().stream()
                    .filter(seat -> seat.getSeatRow() == seatDto.getSeatRow() && seat.getSeatColumn() == seatDto.getSeatColumn())
                    .findFirst()
                    .orElse(null);

            if (existingSeat == null) {
                // If the seat does not exist, create a new one and add it to the room
                SeatDto seat = seatService.createSeat(seatDto);
                room.getSeats().add(modelMapper.map(seat, Seat.class));
                //tang so luong ghe

            } else {
                // If the seat already exists, remove it from the seatsToRemove set
                seatsToRemove.remove(existingSeat);
            }
        }

        // Remove excess seats from the room
        for (Seat seatToRemove : seatsToRemove) {
            room.getSeats().remove(seatToRemove);
            seatService.deleteSeatById(seatToRemove.getId());
        }

        if(!roomDto.getName().isEmpty() && !roomDto.getName().isBlank()) {
            room.setName(roomDto.getName());
        }else{
            room.setName(room.getName());
        }
        if(roomDto.isStatus() != room.isStatus()){
            room.setStatus(roomDto.isStatus());
        }else {
            room.setStatus(room.isStatus());
        }
        if(roomDto.getPrice() != room.getPrice() && roomDto.getPrice() >= 0){
            room.setPrice(roomDto.getPrice());
        }else {
            room.setPrice(room.getPrice());
        }
        if(!roomDto.getType().isEmpty() && !roomDto.getType().isBlank()) {
            if (roomDto.getType().equals("2D")) {
                room.setType(ETypeRoom.ROOM2D);
            } else if (roomDto.getType().equals("3D")) {
                room.setType(ETypeRoom.ROOM3D);
            } else {
                room.setType(ETypeRoom.ROOM4D);
            }
        }else{
            room.setType(room.getType());
        }
            room.setTotalSeats(room.getSeats().size());
        // Save the updated room
        roomRepository.save(room);
        // Map Room to RoomDto and return
        modelMapper.map(room, RoomDto.class);
    }

    @Override
    public RoomDto getRoomById(Long id) {
        // Retrieve the room
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found with id:" + id, HttpStatus.NOT_FOUND));
        // Map Room to RoomDto and return
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Room not found with id:" + id, HttpStatus.NOT_FOUND));
        // Delete the room
        roomRepository.delete(room);
        cinemaService.countTotalRooms(room.getCinema().getId(), -1);
    }
    @Override
    public List<RoomDto> getAllRoomsPage(Integer page, Integer size, String code, String name, Long cinemaId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Room> roomPage;
        if (code != null && !code.isEmpty()) {
            roomPage = roomRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            roomPage = roomRepository.findByNameContaining(name, pageable);
        } else if (cinemaId != null && cinemaId != 0) {
            roomPage = roomRepository.findByCinemaId(cinemaId, pageable);
        } else {
            roomPage = roomRepository.findAll(pageable);
        }
        //sort by created date
        return roomPage.stream().sorted(Comparator.comparing(Room::getCreatedDate).reversed())
                .map(room -> modelMapper.map(room, RoomDto.class))
                .toList();
    }

    @Override
    public long countAllRooms(String code, String name, Long cinemaId) {
        if (code != null && !code.isEmpty()) {
            return roomRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return roomRepository.countByNameContaining(name);
        } else if (cinemaId != null && cinemaId != 0) {
            return roomRepository.countByCinemaId(cinemaId);
        } else {
            return roomRepository.count();
        }
    }
}
