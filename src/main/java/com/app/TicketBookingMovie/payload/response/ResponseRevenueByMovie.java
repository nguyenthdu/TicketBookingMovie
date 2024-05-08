package com.app.TicketBookingMovie.payload.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResponseRevenueByMovie {
    private String code;//mã phim
    private String name;//tên phim
    private String image;
    private int totalInvoice;//tổng số lượng hóa đơn của phim
    private int totalTicket;//tổng số lượng vé của phim
    private BigDecimal totalRevenue;//tổng doanh thu của phim( tổng tiền của các hóa đơn)
}
