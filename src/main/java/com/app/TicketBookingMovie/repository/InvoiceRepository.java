package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Page<Invoice> findByCode(String invoiceCode, Pageable pageable);

    //lấy từ  hóa đơn từ chi tiết vé hóa đơn->vé->lịch chiếu->phòng->rạp
    @Query("SELECT i FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.room c JOIN c.cinema a WHERE a.id = ?1")
    Page<Invoice> findByCinemaId(Long cinemaId, Pageable pageable);

    @Query("SELECT i FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.room c WHERE c.id = ?1")
    Page<Invoice> findByRoomId(Long roomId, Pageable pageable);

    @Query("SELECT i FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.movie m WHERE m.id = ?1")
    Page<Invoice> findByMovieId(Long movieId, Pageable pageable);

    @Query("SELECT i FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r WHERE r.code = ?1")
    Page<Invoice> findByShowTimeCode(String showTimeCode, Pageable pageable);

    Page<Invoice> findByStaffId(Long staffId, Pageable pageable);

    Page<Invoice> findByUserId(Long userId, Pageable pageable);

    Page<Invoice> findByStatus(boolean aTrue, Pageable pageable);


    long countByCode(String invoiceCode);

    @Query("SELECT COUNT(i) FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.room c JOIN c.cinema a WHERE a.id = ?1")
    long countByCinemaId(Long cinemaId);

    @Query("SELECT COUNT(i) FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.room c WHERE c.id = ?1")
    long countByRoomId(Long roomId);

    @Query("SELECT COUNT(i) FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r JOIN r.movie m WHERE m.id = ?1")
    long countByMovieId(Long movieId);

    @Query("SELECT COUNT(i) FROM Invoice i JOIN i.invoiceTicketDetails t JOIN t.ticket s JOIN s.showTime r WHERE r.code = ?1")
    long countByShowTimeCode(String showTimeCode);

    long countByStaffId(Long staffId);

    long countByUserId(Long userId);

    long countByStatus(boolean aTrue);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE DATE(i.createdDate) = DATE(?1)")
    long countByCreatedDate(LocalDate dateCreated);

    //chỉ cần giống ngày tháng năm không cần giờ phút giây
    @Query("SELECT i FROM Invoice i WHERE DATE(i.createdDate) = DATE(?1)")
    Page<Invoice> findByCreatedDate(LocalDate dateCreatedStart, Pageable pageable);

    @Query("SELECT i FROM Invoice i WHERE DATE(i.createdDate) = DATE(?1)")
    List<Invoice> findInvoiceByToday(LocalDate localDate);
}
