package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@RequestMapping("api/showtime")
@RestController
public class ShowTimeController {
    private final ShowTimeService showTimeService;

    public ShowTimeController(ShowTimeService showTimeService) {
        this.showTimeService = showTimeService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createShowTime(@RequestBody Set<ShowTimeDto> showTimeDtos) {
        try {
            showTimeService.createShowTime(showTimeDtos);
            return ResponseEntity.ok(new MessageResponseDto("Show time created successfully", HttpStatus.CREATED.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShowTimeDto> getShowTimeById(@PathVariable Long id) {
        ShowTimeDto showTimeDto = showTimeService.getShowTimeById(id);
        return ResponseEntity.ok(showTimeDto);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ShowTimeDto>> getAllShowTimes(@RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size,
                                                                     @RequestParam(required = false) String code,
                                                                     @RequestParam("movieId") Long movieId,
                                                                     @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                     @RequestParam(required = false) Long roomId) {
        PageResponse<ShowTimeDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(showTimeService.getAllShowTimes(page, size, code, movieId, date, roomId));
        pageResponse.setTotalElements(showTimeService.countAllShowTimes(code, movieId, date, roomId));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateShowTime(
            @RequestParam("id") Long id,
            @RequestParam("showDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate showDate,
            @RequestParam("showTime") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime showTime,
            @RequestParam("movieId") Long movieId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("status") boolean status) {
        ShowTimeDto showTimeDto = new ShowTimeDto();
        showTimeDto.setId(id);
        showTimeDto.setShowDate(showDate);
        showTimeDto.setShowTime(showTime);
        showTimeDto.setMovieId(movieId);
        showTimeDto.setRoomId(roomId);
        showTimeDto.setStatus(status);

        try {
            showTimeService.updateShowTime(showTimeDto);
            return ResponseEntity.ok(new MessageResponseDto("Show time updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteShowTime(@PathVariable Long id) {
        try {
            showTimeService.deleteShowTime(id);
            return ResponseEntity.ok(new MessageResponseDto("Show time deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

}
