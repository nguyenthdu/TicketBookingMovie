package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.security.JwtUtils;
import com.app.TicketBookingMovie.services.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createInvoice(@RequestParam("showTimeId") Long showTimeId,
                                                @RequestParam("seatIds") Set<Long> seatIds,
                                                @RequestParam("foodIds") List<Long> foodIds,
                                                @RequestParam(value = "emailUser", required = false) String emailUser,
                                                @RequestParam("staffId") Long staffId,
                                                @RequestParam("promotionIds") Set<Long> promotionIds
            , HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromCookies(request);
        if (emailUser.isEmpty()) {
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                emailUser = jwtUtils.getEmailFromJwtToken(jwt);
            }
        }
        try {
            invoiceService.createInvoice(showTimeId, seatIds, foodIds, emailUser, staffId, promotionIds);
            return ResponseEntity.ok("Invoice created successfully");
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

}

