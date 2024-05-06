package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.RoomDto;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("api/room")
public class RoomController {
    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createRoom(@RequestBody RoomDto roomDto) {
        try {
            roomService.createRoom(roomDto);
            return ResponseEntity.ok().body(new MessageResponse("Room created successfully: " + roomDto.getName(), HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateRoom(@PathVariable Long id, @RequestBody RoomDto roomDto) {
        roomDto.setId(id);
        try {
            roomService.updateRoom(roomDto);
            return ResponseEntity.ok().body(new MessageResponse("Room updated successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDto> getRoomById(@PathVariable Long id) {
        return new ResponseEntity<>(roomService.getRoomById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteRoom(@PathVariable Long id) {
        try {
            roomService.deleteRoom(id);
            return ResponseEntity.ok().body(new MessageResponse("Room deleted successfully with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
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

