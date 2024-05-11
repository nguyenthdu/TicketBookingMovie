package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.services.TypeSeatService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("api/typeSeat")
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
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

//    @PutMapping
//    public ResponseEntity<MessageResponse> updateTypeSeatById(
//            @RequestParam("id") Long id,
//            @RequestParam("price") double price) {
//        TypeSeatDto typeSeatDto = new TypeSeatDto();
//        typeSeatDto.setId(id);
////        typeSeatDto.setPrice(price);
//        try {
//            typeSeatService.updateTypeSeatById(typeSeatDto);
//            return ResponseEntity.ok(new MessageResponse("Cập nhật loại ghế thành công.", HttpStatus.OK.value(), Instant.now().toString()));
//        } catch (AppException e) {
//            return ResponseEntity.ok(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
//        }
//    }

    @GetMapping
    public ResponseEntity<Set<TypeSeatDto>> getAllTypeSeats() {
        return ResponseEntity.ok(typeSeatService.getAllTypeSeats());
    }
}
