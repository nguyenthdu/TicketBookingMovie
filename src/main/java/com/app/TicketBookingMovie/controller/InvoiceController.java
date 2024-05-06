package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.InvoiceDto;
import com.app.TicketBookingMovie.dtos.ResponeInvoiceDetail;
import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.InvoiceService;
import com.app.TicketBookingMovie.services.ReturnInvoviceService;
import com.app.TicketBookingMovie.services.VNPAYService;
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
    private final ReturnInvoviceService returnInvoviceService;
    private final VNPAYService vnPayService;

    public InvoiceController(InvoiceService invoiceService, JwtUtils jwtUtils, ReturnInvoviceService returnInvoviceService, VNPAYService vnPayService) {
        this.invoiceService = invoiceService;
        this.jwtUtils = jwtUtils;
        this.returnInvoviceService = returnInvoviceService;
        this.vnPayService = vnPayService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createInvoice(@RequestParam("showTimeId") Long showTimeId,
                                                         @RequestParam("seatIds") Set<Long> seatIds,
                                                         @RequestParam(value = "foodIds", required = false) List<Long> foodIds,
                                                         @RequestParam(value = "emailUser") String emailUser,
                                                         @RequestParam(value = "staffId", required = false) Long staffId,
                                                         @RequestParam("typePay") String typePay) {

        try {
            invoiceService.createInvoice(showTimeId, seatIds, foodIds, emailUser, staffId, typePay);
            return ResponseEntity.ok(new MessageResponse("Tạo hóa đơn thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
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
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalDate endDate
    ) {
        PageResponse<InvoiceDto> invoices = new PageResponse<>();
        invoices.setContent(invoiceService.getAllInvoices(page, size, invoiceCode, cinemaId, roomId, movieId, showTimeCode, staffId, userId, startDate, endDate));
        invoices.setTotalElements(invoiceService.countAllInvoices(invoiceCode, cinemaId, roomId, movieId, showTimeCode, staffId, userId, startDate, endDate));
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

    @PostMapping("cancel")
    public ResponseEntity<MessageResponse> cancelInvoice(@RequestParam("invoiceId") Long invoiceId,
                                                         @RequestParam("reason") String reason) {
        ReturnInvoiceDto returnInvoiceDto = new ReturnInvoiceDto();
        returnInvoiceDto.setInvoiceId(invoiceId);
        returnInvoiceDto.setReason(reason);
        try {
            returnInvoviceService.cancelInvoice(returnInvoiceDto);
            return ResponseEntity.ok(new MessageResponse("Hủy hóa đơn thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }

    }

    @GetMapping("return")
    public ResponseEntity<PageResponse<ReturnInvoiceDto>> getAllReturnInvoices(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "userCode", required = false) String userCode,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalDate endDate
    ) {
        PageResponse<ReturnInvoiceDto> invoices = new PageResponse<>();
        invoices.setContent(returnInvoviceService.getAllReturnInvoice(page, size, code, userCode, startDate, endDate));
        invoices.setTotalElements(returnInvoviceService.countAllReturnInvoice(code, userCode, startDate, endDate));
        invoices.setTotalPages((int) Math.ceil((double) invoices.getTotalElements() / size));
        invoices.setCurrentPage(page);
        invoices.setPageSize(size);
        return ResponseEntity.ok(invoices);

    }

    @GetMapping("return/{id}")
    public ResponseEntity<ReturnInvoiceDto> getReturnInvoiceById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(returnInvoviceService.getReturnInvoice(id));
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(null);
        }

    }

    @PostMapping("vnpay")
    public ResponseEntity<MessageResponse> submitOrder(@RequestParam("amount") int orderTotal,
                                                       @RequestParam("showTimeId") Long showTimeId,
                                                       @RequestParam("seatIds") Set<Long> seatIds,
                                                       @RequestParam(value = "foodIds", required = false) List<Long> foodIds,
                                                       @RequestParam(value = "emailUser") String emailUser,
                                                       @RequestParam(value = "staffId", required = false) Long staffId,
                                                       HttpServletRequest request) {
        String vnpayUrl = vnPayService.createOrder(request, orderTotal, showTimeId, seatIds, foodIds, emailUser, staffId);
        return ResponseEntity.ok().body(new MessageResponse(vnpayUrl, HttpStatus.OK.value(), Instant.now().toString()));
    }

    @GetMapping("vnpay-payment-return")
    public ResponseEntity<MessageResponse> paymentCompleted(HttpServletRequest request) {
        int paymentStatus = vnPayService.orderReturn(request);
        if (paymentStatus == 1) {
            return ResponseEntity.ok().body(new MessageResponse("Thanh toán thành công", HttpStatus.OK.value(), Instant.now().toString()));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Thanh toán thất bại", HttpStatus.BAD_REQUEST.value(), Instant.now().toString()));
        }
    }


}

