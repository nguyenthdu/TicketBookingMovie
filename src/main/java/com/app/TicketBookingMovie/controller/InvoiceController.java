package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.InvoiceDto;
import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.dtos.ResponeInvoiceDetail;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/invoice")
public class InvoiceController {
    JwtUtils jwtUtils;
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService, JwtUtils jwtUtils) {
        this.invoiceService = invoiceService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createInvoice(@RequestParam("showTimeId") Long showTimeId,
                                                            @RequestParam("seatIds") Set<Long> seatIds,
                                                            @RequestParam(value = "foodIds", required = false) List<Long> foodIds,
                                                            @RequestParam(value = "emailUser", required = false) String emailUser,
                                                            @RequestParam("staffId") Long staffId

            , HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        if (emailUser.isEmpty()) {
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                emailUser = jwtUtils.getEmailFromJwtToken(jwt);
            }
        }

        try {
            invoiceService.createInvoice(showTimeId, seatIds, foodIds, emailUser, staffId);
            return ResponseEntity.ok(new MessageResponseDto("Tạo hóa đơn thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(invoiceService.getInvoiceById(id));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(null);
        }

    }

    @GetMapping
    public ResponseEntity<PageResponse<InvoiceDto>> getAllInvoices(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "invoiceCode", required = false) String invoiceCode,
            @RequestParam(value = "cinemaId", required = false) Long cinemaId,
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestParam(value = "movieId", required = false) Long movieId,
            @RequestParam(value = "showTimeCode", required = false) String showTimeCode,
            @RequestParam(value = "staffId", required = false) Long staffId,
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "dateCreated", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalDate dateCreated) {
        PageResponse<InvoiceDto> invoices = new PageResponse<>();
        invoices.setContent(invoiceService.getAllInvoices(page, size, invoiceCode, cinemaId, roomId, movieId, showTimeCode, staffId, userId, status, dateCreated));
        invoices.setTotalElements(invoiceService.countAllInvoices(invoiceCode, cinemaId, roomId, movieId, showTimeCode, staffId, userId, status, dateCreated));
        invoices.setTotalPages((int) Math.ceil((double) invoices.getTotalElements() / size));
        invoices.setCurrentPage(page);
        invoices.setPageSize(size);
        return ResponseEntity.ok(invoices);

    }

    @GetMapping("detail/{id}")
    public ResponseEntity<ResponeInvoiceDetail> getInvoiceDetails(@PathVariable Long id) {
        // Gọi tất cả các phương thức và tạo đối tượng InvoiceDetailsDto
        ResponeInvoiceDetail invoiceDetails = new ResponeInvoiceDetail();
        invoiceDetails.setCinemaDto(invoiceService.getCinemaByInvoiceId(id));
        invoiceDetails.setRoomDto(invoiceService.getRoomByInvoiceId(id));
        invoiceDetails.setMovieDto(invoiceService.getMovieByInvoiceId(id));
        invoiceDetails.setShowTimeDto(invoiceService.getShowTimeByInvoiceId(id));
        invoiceDetails.setUserDto(invoiceService.getUserByInvoiceId(id));
        invoiceDetails.setInvoiceFoodDetailDtos(invoiceService.getInvoiceFoodDetailByInvoiceId(id));
        invoiceDetails.setInvoiceTicketDetailDtos(invoiceService.getInvoiceTicketDetailByInvoiceId(id));
        return new ResponseEntity<>(invoiceDetails, HttpStatus.OK);
    }

}

