package com.app.TicketBookingMovie.payload.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResponseRevenueByCinema {
    private String code;//mã rạp
    private String name;//tên rạp
    private String address;//địa chỉ rạp
    private int totalInvoice;//tổng số lượng hóa đơn của rạp
    private int totalTicket;//tổng số lượng vé của rạp
    private BigDecimal totalRevenue;//tổng doanh thu của rạp( tổng tiền của các hóa đơn)
}
