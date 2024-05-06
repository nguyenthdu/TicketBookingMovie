package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.RoomDto;
import com.app.TicketBookingMovie.dtos.SeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.models.PriceDetail;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new AppException("Không tìm thấy rạp với id:" + roomDto.getCinemaId(), HttpStatus.NOT_FOUND));
        //nếu tên rạp trong cinema đã tồn tại thì thông báo lỗi
        if (roomRepository.findByName(roomDto.getName()).isPresent()) {
            throw new AppException("Tên phòng : " + roomDto.getName() + " đã tồn tại", HttpStatus.BAD_REQUEST);
        }
        room.setCinema(cinema);
        //handle type
        getTypeRoom(roomDto, room);
        // Create seats and add them to the room
        Set<Seat> seats = new HashSet<>();
        for (SeatDto seatDto : roomDto.getSeats()) {
            SeatDto seat = seatService.createSeat(seatDto);
            seats.add(modelMapper.map(seat, Seat.class));
        }
        room.setSeats(seats);
        room.setTotalSeats(room.getSeats().size());
        if (!cinema.isStatus() && room.isStatus()) {
            throw new AppException("Không thể đặt trạng thái phòng hoạt động khi trạng thái rạp không hoạt động!!!", HttpStatus.BAD_REQUEST);
        }
        room.setCreatedDate(LocalDateTime.now());
        roomRepository.save(room);
        cinemaService.countTotalRooms(roomDto.getCinemaId(), 1);
        // Create seats and add them to the room
        modelMapper.map(room, RoomDto.class);
    }

    public String randomCode() {
        return "PH" + LocalDateTime.now().getNano();

    }

    @Override
    public void updateRoom(RoomDto roomDto) {
        // Retrieve the existing Room
        Room room = roomRepository.findById(roomDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với  id:" + roomDto.getId(), HttpStatus.NOT_FOUND));
        //nếu tên rạp trong cinema đã tồn tại thì thông báo lỗi
        if (roomRepository.findByName(roomDto.getName()).isPresent()) {
            throw new AppException("Tên phòng: " + roomDto.getName() + " đã tồn tại", HttpStatus.BAD_REQUEST);
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

        if (!roomDto.getName().isEmpty() && !roomDto.getName().isBlank()) {
            room.setName(roomDto.getName());
        } else {
            room.setName(room.getName());
        }
        if (roomDto.isStatus() != room.isStatus()) {
            room.setStatus(roomDto.isStatus());
        } else {
            room.setStatus(room.isStatus());
        }

        if (!roomDto.getType().isEmpty() && !roomDto.getType().isBlank()) {
            getTypeRoom(roomDto, room);
        } else {
            room.setType(room.getType());
        }
        room.setTotalSeats(room.getSeats().size());
        // Save the updated room
        roomRepository.save(room);
        // Map Room to RoomDto and return
        modelMapper.map(room, RoomDto.class);
    }

    private void getTypeRoom(RoomDto roomDto, Room room) {
        switch (roomDto.getType()) {
            case "3D":
                room.setType(ETypeRoom.ROOM3D);
                break;
            case "4D":
                room.setType(ETypeRoom.ROOM4D);
                break;
            default:
                room.setType(ETypeRoom.ROOM2D);
        }
    }

    @Override
    public RoomDto getRoomById(Long id) {
        // Retrieve the room
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với id:" + id, HttpStatus.NOT_FOUND));
        // Map Room to RoomDto and return
        RoomDto roomDto = new RoomDto();
        roomDto.setId(room.getId());
        roomDto.setCode(room.getCode());
        roomDto.setName(room.getName());
        roomDto.setType(room.getType().name());
        roomDto.setTotalSeats(room.getTotalSeats());
        roomDto.setStatus(room.isStatus());
        roomDto.setSeats(room.getSeats().stream().map(seat -> modelMapper.map(seat, SeatDto.class)).collect(Collectors.toSet()));
        roomDto.setCreatedDate(room.getCreatedDate());
        //lấy giá
        room.getPriceDetails().stream().filter(PriceDetail::isStatus).findFirst()
                .ifPresent(priceDetail -> {
                    roomDto.setPrice(priceDetail.getPrice());
                    roomDto.setActive_price(priceDetail.isStatus());
                });
// Map each seat to a SeatDto and set the price according to the chair type
        roomDto.setSeats(room.getSeats().stream().map(seat -> {
            SeatDto seatDto = modelMapper.map(seat, SeatDto.class);
            seat.getSeatType().getPriceDetails().stream().findFirst().ifPresent(priceDetail -> {
                seatDto.setPrice(priceDetail.getPrice());
            });
            return seatDto;
        }).collect(Collectors.toSet()));

        roomDto.setCinemaName(room.getCinema().getName());
        roomDto.setCinemaId(room.getCinema().getId());
        return roomDto;
    }

    @Override
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với id:" + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy phòng với id:" + id, HttpStatus.NOT_FOUND));
        // Delete the room
        roomRepository.delete(room);
        cinemaService.countTotalRooms(room.getCinema().getId(), -1);
    }

    @Override
    public List<RoomDto> getAllRoomsPage(Integer page, Integer size, String code, String name, Long cinemaId) {
        List<Room> roomPage = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (code != null && !code.isEmpty()) {
            roomPage = roomPage.stream().filter(room -> room.getCode().equals(code)).collect(Collectors.toList());
        } else if (name != null && !name.isEmpty()) {
            roomPage = roomPage.stream().filter(room -> room.getName().toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
        } else if (cinemaId != null && cinemaId != 0) {
            roomPage = roomPage.stream().filter(room -> room.getCinema().getId().equals(cinemaId)).collect(Collectors.toList());
        }
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, roomPage.size());
        return roomPage.subList(fromIndex, toIndex).stream()
                .map(room -> {
                    RoomDto roomDto = new RoomDto();
                    roomDto.setId(room.getId());
                    roomDto.setCode(room.getCode());
                    roomDto.setName(room.getName());
                    room.getPriceDetails().stream().filter(PriceDetail::isStatus).findFirst().ifPresent(priceDetail -> {
                        roomDto.setPrice(priceDetail.getPrice());
                        roomDto.setActive_price(priceDetail.isStatus());
                    });
                    roomDto.setType(room.getType().name());
                    roomDto.setTotalSeats(room.getTotalSeats());
                    roomDto.setStatus(room.isStatus());
//                    roomDto.setSeats(room.getSeats().stream().map(seat -> modelMapper.map(seat, SeatDto.class)).collect(Collectors.toSet()));
                    roomDto.setSeats(null);
                    roomDto.setCreatedDate(room.getCreatedDate());
                    roomDto.setCinemaName(room.getCinema().getName());
                    roomDto.setCinemaId(room.getCinema().getId());
                    return roomDto;
                }).toList();
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
