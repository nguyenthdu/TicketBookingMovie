package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.TicketDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<?> createTickets(@RequestParam("showTimeId") Long showTimeId,
                                           @RequestParam("seatIds") Set<Long> seatIds) {
        TicketDto ticketDto = new TicketDto();
        ticketDto.setShowTimeId(showTimeId);
        ticketDto.setSeatIds(seatIds);

        try {
            ticketService.createTickets(ticketDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Tickets created successfully!");
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred!");
        }
    }
}
