package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByCinema;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByMovie;
import com.app.TicketBookingMovie.payload.response.ResponseRevenueByUser;
import com.app.TicketBookingMovie.payload.response.ResponseRevenuePromotionLine;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

public interface StatisticalService {
    List<ResponseRevenueByCinema> getRevenueByCinema(Integer page, Integer size, String cinemaCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection);

    List<ResponseRevenueByMovie> getRevenueByMovie(Integer page, Integer size, String movieCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection);

    List<ResponseRevenueByUser> getRevenueByUser(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection);

    //thống kê doanh thu của nhân viên
    List<ResponseRevenueByUser> getRevenueByStaff(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection);

    //thống kê hóa đơn hủy
    List<ReturnInvoiceDto> getReturnInvoice(Integer page, Integer size, String code, String userCode, LocalDate startDate, LocalDate endDate,String sortType, Sort.Direction sortDirection);
    List<ResponseRevenuePromotionLine> getRevenueByPromotionLine(Integer page, Integer size, String promotionLineCode, LocalDate startDate, LocalDate endDate, String sortType, Sort.Direction sortDirection);
}

