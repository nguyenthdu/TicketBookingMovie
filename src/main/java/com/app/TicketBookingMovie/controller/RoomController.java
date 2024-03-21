package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.RoomDto;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/room")
public class RoomController {
    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createRoom(@RequestBody RoomDto roomDto) {
        try {
            roomService.createRoom(roomDto);
            return ResponseEntity.ok().body(new MessageResponseDto("Room created successfully: " + roomDto.getName(), HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponseDto> updateRoom(@PathVariable Long id, @RequestBody RoomDto roomDto) {
        roomDto.setId(id);
        try {
            roomService.updateRoom(roomDto);
            return ResponseEntity.ok().body(new MessageResponseDto("Room updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return new ResponseEntity<>(roomService.getRoomById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok().body(new MessageResponseDto("Room deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<RoomDto>> getAllRoomsPage(@RequestParam(defaultValue = "0") Integer page,
                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                 @RequestParam(required = false) String code,
                                                                 @RequestParam(required = false) String name,
                                                                 @RequestParam(required = false) Long cinemaId) {
        PageResponse<RoomDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(roomService.getAllRoomsPage(page, size, code, name, cinemaId));
        pageResponse.setTotalElements(roomService.countAllRooms(code, name, cinemaId));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}

