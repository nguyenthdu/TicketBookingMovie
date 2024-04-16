package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;

public interface ReturnInvoviceService {
    void cancelInvoice(ReturnInvoiceDto returnInvoiceDto);
}
