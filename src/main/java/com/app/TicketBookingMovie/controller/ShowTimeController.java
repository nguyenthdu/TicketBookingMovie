package com.app.TicketBookingMovie.controller;


import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.ShowTimeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Set;

@RequestMapping("api/showtime")
@RestController
public class ShowTimeController {
    private final ShowTimeService showTimeService;

    public ShowTimeController(ShowTimeService showTimeService) {
        this.showTimeService = showTimeService;
    }

    @PostMapping
    public ResponseEntity<Set<ShowTimeDto>> createShowTime(@RequestBody Set<ShowTimeDto> showTimeDtos) {
        Set<ShowTimeDto> createdShowTimes = showTimeService.createShowTime(showTimeDtos);
        return ResponseEntity.ok(createdShowTimes);
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
                                                                     @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        PageResponse<ShowTimeDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(showTimeService.getAllShowTimes(page, size, code, movieId, date));
        pageResponse.setTotalElements(showTimeService.countAllShowTimes(code, movieId, date));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

}
