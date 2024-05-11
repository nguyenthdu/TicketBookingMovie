package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.dtos.ShowTimeSeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RequestMapping("api/showtime")
@RestController
public class ShowTimeController {
    private final ShowTimeService showTimeService;

    public ShowTimeController(ShowTimeService showTimeService) {
        this.showTimeService = showTimeService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> createShowTime(@RequestBody Set<ShowTimeDto> showTimeDtos) {
        try {
            showTimeService.createShowTime(showTimeDtos);
            return ResponseEntity.ok(new MessageResponse("Tạo lịch chiếu thành công.", HttpStatus.CREATED.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<ShowTimeDto> getShowTimeById(@PathVariable Long id) {
        ShowTimeDto showTimeDto = showTimeService.getShowTimeById(id);
        return ResponseEntity.ok(showTimeDto);
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<PageResponse<ShowTimeDto>> getAllShowTimes(@RequestParam(defaultValue = "0") Integer page,
                                                                     @RequestParam(defaultValue = "10") Integer size,
                                                                     @RequestParam(required = false) String code,
                                                                     @RequestParam("movieId") Long movieId,
                                                                     @RequestParam("cinemaId") Long cinemaId,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                     @RequestParam(required = false) Long roomId) {
        PageResponse<ShowTimeDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(showTimeService.getAllShowTimes(page, size, code, cinemaId, movieId, date, roomId));
        pageResponse.setTotalElements(showTimeService.countAllShowTimes(code, cinemaId, movieId, date, roomId));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> updateShowTime(
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
            return ResponseEntity.ok(new MessageResponse("Cập nhật lịch chiếu thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteShowTime(@PathVariable Long id) {
        try {
            showTimeService.deleteShowTime(id);
            return ResponseEntity.ok(new MessageResponse("Xóa lịch chiếu thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/seat/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<List<ShowTimeSeatDto>> getShowTimeSeatById(@PathVariable Long id) {
        List<ShowTimeSeatDto> showTimeSeatDtos = showTimeService.getShowTimeSeatById(id);
        return ResponseEntity.ok(showTimeSeatDtos);
    }

    @GetMapping("/dates")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<Set<LocalDate>> getShowDatesByMovieId(@RequestParam Long movieId, @RequestParam Long cinemaId) {
        Set<LocalDate> showDates = showTimeService.getShowDatesByMovieId(movieId, cinemaId);
        return ResponseEntity.ok(showDates);
    }

    @PostMapping("/seat")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<MessageResponse> updateStatusHoldSeat(@RequestParam Set<Long> seatIds,
                                                                @RequestParam Long showTimeId,
                                                                @RequestParam boolean status) {
        try {
            showTimeService.updateStatusHoldSeat(seatIds, showTimeId, status);
            return ResponseEntity.ok(new MessageResponse("Cập nhật trạng thái giữ ghế thành công!!!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/seat")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR') or hasRole('USER')")
    public ResponseEntity<MessageResponse> checkSeatStatus(@RequestParam Set<Long> seatIds, @RequestParam Long showTimeId) {
        String message = showTimeService.checkSeatStatus(seatIds, showTimeId);
        return ResponseEntity.ok(new MessageResponse(message, HttpStatus.OK.value(), Instant.now().toString()));
    }
}
