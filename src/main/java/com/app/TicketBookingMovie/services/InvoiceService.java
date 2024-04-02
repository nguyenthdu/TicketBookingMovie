package com.app.TicketBookingMovie.services;

import java.util.List;
import java.util.Set;

public interface InvoiceService {
    //viết phương thức tạo hóa đơn
    void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId,Set<Long> promotionLineIds);
}
