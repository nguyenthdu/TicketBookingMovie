package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.services.SeatService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("api/seat")
public class SeatController {
    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    //    @PostMapping
//    public ResponseEntity<List<SeatDto>> createSeats(List<SeatDto> seatDtos) {
//        return new ResponseEntity<>(seatService.createSeats(seatDtos), HttpStatus.CREATED);
//
//    }

}
