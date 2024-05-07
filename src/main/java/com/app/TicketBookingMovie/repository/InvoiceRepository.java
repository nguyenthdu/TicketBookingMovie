package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

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

    @Query("SELECT COUNT(i) FROM Invoice i WHERE DATE(i.createdDate) BETWEEN DATE(?1) AND DATE(?2)")
    long countByCreatedDate(LocalDate startDate, LocalDate endDate);

    @Query("SELECT i FROM Invoice i WHERE DATE(i.createdDate) = DATE(?1)")
    List<Invoice> findInvoiceByToday(LocalDate localDate);

    List<Invoice> findByUserId(Long id);
}
