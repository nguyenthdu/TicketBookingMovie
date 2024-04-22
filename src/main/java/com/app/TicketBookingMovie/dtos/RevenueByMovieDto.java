package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueByMovieDto {
    private String code;//mã phim
    private String name;//tên phim
    private String image;
    private int totalInvoice;//tổng số lượng hóa đơn của phim
    private int totalTicket;//tổng số lượng vé của phim
    private BigDecimal totalRevenue;//tổng doanh thu của phim( tổng tiền của các hóa đơn)
}
