package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.dtos.RevenueByCinemaDto;
import com.app.TicketBookingMovie.dtos.RevenueByMovieDto;
import com.app.TicketBookingMovie.dtos.RevenueByUserDto;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.StatisticalService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/statistical")
public class StatisticalController {
    private final StatisticalService statisticalService;

    public StatisticalController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    @GetMapping("/revenue-by-cinema")
    public ResponseEntity<PageResponse<RevenueByCinemaDto>> getRevenueByCinema(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                               @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                               @RequestParam(required = false) String cinemaCode,
                                                                               @RequestParam LocalDate startDate,
                                                                               @RequestParam LocalDate endDate,
                                                                               @RequestParam(required = false) Sort.Direction sortDirection,
                                                                               @RequestParam(required = false) String sortType) {
        PageResponse<RevenueByCinemaDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByCinema(page, size, cinemaCode, startDate, endDate, sortType, sortDirection));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/revenue-by-movie")
    public ResponseEntity<PageResponse<RevenueByMovieDto>> getRevenueByMovie(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                             @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                             @RequestParam(required = false) String movieCode,
                                                                             @RequestParam LocalDate startDate,
                                                                             @RequestParam LocalDate endDate,
                                                                             @RequestParam(required = false) Sort.Direction sortDirection,
                                                                             @RequestParam(required = false) String sortType) {

        PageResponse<RevenueByMovieDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByMovie(page, size, movieCode, startDate, endDate, sortType, sortDirection));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

    @GetMapping("/revenue-by-user")
    public ResponseEntity<PageResponse<RevenueByUserDto>> getRevenueByUser(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                           @RequestParam(required = false) String userCode,
                                                                           @RequestParam(required = false) String email,
                                                                           @RequestParam(required = false) String phone,
                                                                           @RequestParam LocalDate startDate,
                                                                           @RequestParam LocalDate endDate,
                                                                           @RequestParam(required = false) Sort.Direction sortDirection,
                                                                           @RequestParam(required = false) String sortType) {

        PageResponse<RevenueByUserDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByUser(page, size, userCode, email, phone, startDate, endDate, sortType, sortDirection));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/revenue-by-staff")
    public ResponseEntity<PageResponse<RevenueByUserDto>> getRevenueByStaff(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                            @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                            @RequestParam(required = false) String userCode,
                                                                            @RequestParam(required = false) String email,
                                                                            @RequestParam(required = false) String phone,
                                                                            @RequestParam LocalDate startDate,
                                                                            @RequestParam LocalDate endDate,
                                                                            @RequestParam(required = false) Sort.Direction sortDirection,
                                                                            @RequestParam(required = false) String sortType) {

        PageResponse<RevenueByUserDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByStaff(page, size, userCode, email, phone, startDate, endDate, sortType, sortDirection));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/return-invoice")
    public ResponseEntity<PageResponse<ReturnInvoiceDto>> getReturnInvoice(@RequestParam(required = false, defaultValue = "0") Integer page,
                                                                           @RequestParam(required = false, defaultValue = "10") Integer size,
                                                                           @RequestParam(required = false) String code,
                                                                           @RequestParam(required = false) String userCode,
                                                                           @RequestParam LocalDate startDate,
                                                                           @RequestParam LocalDate endDate,
                                                                           @RequestParam(required = false) Sort.Direction sortDirection,
                                                                           @RequestParam(required = false) String sortType) {

        PageResponse<ReturnInvoiceDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getReturnInvoice(page, size, code, userCode, startDate, endDate, sortType, sortDirection));
        pageResponse.setTotalElements(pageResponse.getContent().size());
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}