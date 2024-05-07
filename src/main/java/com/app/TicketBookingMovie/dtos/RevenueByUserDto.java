package com.app.TicketBookingMovie.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RevenueByUserDto {
    private String code;//mã người dùng
    private String name;//tên người dùng
    private String email;//email người dùng
    private String phone;//số điện thoại người dùng
    private int totalInvoice;//tổng số lượng hóa đơn của người dùng
    private int totalTicket;//tổng số lượng vé của người dùng
    private BigDecimal totalDiscount;//tổng giảm giá của người dùng
    private BigDecimal totalRevenue;//tổng doanh thu của người dùng( tổng tiền của các hóa đơn)
}
