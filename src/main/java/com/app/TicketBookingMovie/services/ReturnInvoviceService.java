package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;

import java.time.LocalDate;
import java.util.List;

public interface ReturnInvoviceService {
    void cancelInvoice(ReturnInvoiceDto returnInvoiceDto);
    ReturnInvoiceDto getReturnInvoice(Long invoiceId);
    List<ReturnInvoiceDto> getAllReturnInvoice(Integer page, Integer size, String code, String userCode, LocalDate startDate, LocalDate endDate);
    long countAllReturnInvoice(String code, String userCode, LocalDate startDate, LocalDate endDate);
}
