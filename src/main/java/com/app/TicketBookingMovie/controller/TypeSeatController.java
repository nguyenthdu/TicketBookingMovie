package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.services.TypeSeatService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Set;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/typeSeat")
public class TypeSeatController {
    private final TypeSeatService typeSeatService;

    public TypeSeatController(TypeSeatService typeSeatService) {
        this.typeSeatService = typeSeatService;
    }

    @PostConstruct
    public void init() {
        typeSeatService.createTypeSeat();
    }


    @GetMapping("/{id}")
    public ResponseEntity<TypeSeatDto> getTypeSeatById(@PathVariable Long id) {
        return ResponseEntity.ok(typeSeatService.getTypeSeatById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateTypeSeatById(
            @RequestParam("id") Long id,
            @RequestParam("price") double price) {
        TypeSeatDto typeSeatDto = new TypeSeatDto();
        typeSeatDto.setId(id);
        typeSeatDto.setPrice(price);
        try {
            typeSeatService.updateTypeSeatById(typeSeatDto);
            return ResponseEntity.ok(new MessageResponseDto("Update type seat successfully with: " + typeSeatDto.getId(), HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.ok(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping
    public ResponseEntity<Set<TypeSeatDto>> getAllTypeSeats() {
        return ResponseEntity.ok(typeSeatService.getAllTypeSeats());
    }
}
