package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByCinema;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByMovie;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByUser;
import com.app.TicketBookingMovie.payload.response.ResponseRevenuePromotionLine;
import com.app.TicketBookingMovie.services.StatisticalService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("api/statistical")
@PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
public class StatisticalController {
    private final StatisticalService statisticalService;

    public StatisticalController(StatisticalService statisticalService) {
        this.statisticalService = statisticalService;
    }

    @GetMapping("/revenue-by-cinema")
    public ResponseEntity<PageResponse<ResponseRevenueByCinema>> getRevenueByCinema(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String cinemaCode,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {
        PageResponse<ResponseRevenueByCinema> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByCinema(page, size, cinemaCode, startDate, endDate,
                sortType, sortDirection));
        pageResponse.setTotalElements(statisticalService.countRevenueByCinema(cinemaCode, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/revenue-by-movie")
    public ResponseEntity<PageResponse<ResponseRevenueByMovie>> getRevenueByMovie(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String movieCode,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {

        PageResponse<ResponseRevenueByMovie> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByMovie(page, size, movieCode, startDate, endDate,
                sortType, sortDirection));
        pageResponse.setTotalElements(statisticalService.countRevenueByMovie(movieCode, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

    @GetMapping("/revenue-by-user")
    public ResponseEntity<PageResponse<ResponseRevenueByUser>> getRevenueByUser(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {

        PageResponse<ResponseRevenueByUser> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByUser(page, size, userCode, email, phone, startDate,
                endDate, sortType, sortDirection));
        pageResponse
                .setTotalElements(statisticalService.countRevenueByUser(userCode, email, phone, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/revenue-by-staff")
    public ResponseEntity<PageResponse<ResponseRevenueByUser>> getRevenueByStaff(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String userCode,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {

        PageResponse<ResponseRevenueByUser> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByStaff(page, size, userCode, email, phone, startDate,
                endDate, sortType, sortDirection));
        pageResponse
                .setTotalElements(statisticalService.countRevenueByStaff(userCode, email, phone, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/return-invoice")
    public ResponseEntity<PageResponse<ReturnInvoiceDto>> getReturnInvoice(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String userCode,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {

        PageResponse<ReturnInvoiceDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getReturnInvoice(page, size, code, userCode, startDate, endDate,
                sortType, sortDirection));
        pageResponse.setTotalElements(statisticalService.countReturnInvoice(code, userCode, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }

    @GetMapping("/revenue-by-promotion-line")
    public ResponseEntity<PageResponse<ResponseRevenuePromotionLine>> getRevenueByPromotionLine(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String promotionLineCode,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Sort.Direction sortDirection,
            @RequestParam(required = false) String sortType) {

        PageResponse<ResponseRevenuePromotionLine> pageResponse = new PageResponse<>();
        pageResponse.setContent(statisticalService.getRevenueByPromotionLine(page, size, promotionLineCode, startDate,
                endDate, sortType, sortDirection));
        pageResponse.setTotalElements(
                statisticalService.countRevenueByPromotionLine(promotionLineCode, startDate, endDate));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}
