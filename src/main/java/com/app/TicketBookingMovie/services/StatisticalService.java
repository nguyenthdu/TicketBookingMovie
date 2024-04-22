package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ReturnInvoiceDto;
import com.app.TicketBookingMovie.dtos.RevenueByCinemaDto;
import com.app.TicketBookingMovie.dtos.RevenueByMovieDto;
import com.app.TicketBookingMovie.dtos.RevenueByUserDto;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;

public interface StatisticalService {
    List<RevenueByCinemaDto> getRevenueByCinema(Integer page, Integer size, String cinemaCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection);

    List<RevenueByMovieDto> getRevenueByMovie(Integer page, Integer size, String movieCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection);

    List<RevenueByUserDto> getRevenueByUser(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection);

    //thống kê doanh thu của nhân viên
    List<RevenueByUserDto> getRevenueByStaff(Integer page, Integer size, String userCode, String email, String phone, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection);

    //thống kê hóa đơn hủy
    List<ReturnInvoiceDto> getReturnInvoice(Integer page, Integer size, String code, String userCode, LocalDate startDate, LocalDate endDate, Sort.Direction sortDirection);
}
