package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface InvoiceService {
    //viết phương thức tạo hóa đơn
    void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId);
    //viết phương thức lấy hóa đơn theo id
    InvoiceDto getInvoiceById(Long id);
    //viết phương thức lấy tất cả hóa đơn
    List<InvoiceDto> getAllInvoices(Integer page, Integer size, String invoiceCode, Long cinemaId, Long  roomId, Long movieId, String showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated);
    long countAllInvoices(String invoiceCode, Long cinemaId, Long  roomId, Long movieId, String showTimeCode, Long staffId, Long userId, String status, LocalDate dateCreated);
    //get detail invoice
    CinemaDto getCinemaByInvoiceId(Long id);
    RoomDto getRoomByInvoiceId(Long id);
    MovieDto getMovieByInvoiceId(Long id);
    ShowTimeDto getShowTimeByInvoiceId(Long id);
    UserDto getUserByInvoiceId(Long id);
    List<InvoiceFoodDetailDto> getInvoiceFoodDetailByInvoiceId(Long id);
    List<InvoiceTicketDetailDto> getInvoiceTicketDetailByInvoiceId(Long id);

}
