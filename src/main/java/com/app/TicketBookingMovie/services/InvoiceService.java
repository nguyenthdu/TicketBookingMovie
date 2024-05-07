package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.*;
import com.app.TicketBookingMovie.models.Invoice;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface InvoiceService {
    //viết phương thức tạo hóa đơn
    void createInvoice(Long showTimeId, Set<Long> seatIds, List<Long> foodIds, String emailUser, Long staffId, String typePay);
    //viết phương thức lấy hóa đơn theo id
    InvoiceDto getInvoiceById(Long id);
    Invoice findById(Long id);
    List<Invoice> findByUserId(Long id);
    void updateStatusInvoice(Long id, boolean status);
    //viết phương thức lấy tất cả hóa đơn
    List<InvoiceDto> getAllInvoices(Integer page, Integer size, String invoiceCode, Long cinemaId, Long  roomId, Long movieId, String showTimeCode, Long staffId, Long userId, LocalDate startDate, LocalDate endDate);
    long countAllInvoices(String invoiceCode, Long cinemaId, Long  roomId, Long movieId, String showTimeCode, Long staffId, Long userId, LocalDate startDate, LocalDate endDate);
    //get detail invoice
    CinemaDto getCinemaByInvoiceId(Long id);
    RoomDto getRoomByInvoiceId(Long id);
    MovieDto getMovieByInvoiceId(Long id);
    ShowTimeDto getShowTimeByInvoiceId(Long id);
    UserDto getUserByInvoiceId(Long id);
    List<InvoiceFoodDetailDto> getInvoiceFoodDetailByInvoiceId(Long id);
    List<InvoiceTicketDetailDto> getInvoiceTicketDetailByInvoiceId(Long id);
    List<PromotionLineDto> getPromotionLineByInvoiceId(Long id);
    //xóa hóa đơn khỏi chương trình khuyến mãi
    void removePromotionLineFromInvoice(Long invoiceId, Long promotionLineId);
    //lấy danh sách tất cả hóa đơn
    List<Invoice> getAll();
}
