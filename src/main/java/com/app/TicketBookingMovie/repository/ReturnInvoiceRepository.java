package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ReturnInvoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ReturnInvoiceRepository extends JpaRepository<ReturnInvoice, Integer> {
    ReturnInvoice findByInvoiceId(Long invoiceId);

    Page<ReturnInvoice> findByCode(String code, Pageable pageable);

    @Query("SELECT r FROM ReturnInvoice r JOIN r.invoice i WHERE i.user = :userCode")
    Page<ReturnInvoice> findByUserCode(String userCode, Pageable pageable);

    Page<ReturnInvoice> findByCancelDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1, Pageable pageable);

    long countByCode(String code);

    @Query("SELECT COUNT(r) FROM ReturnInvoice r JOIN r.invoice i JOIN i.user u WHERE u.code = :userCode")
    long countByUserCode(String userCode);

    long countByCancelDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

    @Query("SELECT r FROM ReturnInvoice r JOIN r.invoice i JOIN i.user u WHERE r.code = ?1 AND DATE(r.cancelDate) BETWEEN DATE(?2) AND DATE(?3)")
    Page<ReturnInvoice> findAllByCodeAndCancelDateBetween(String code, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT r FROM ReturnInvoice r JOIN r.invoice i JOIN i.user u WHERE u.code = ?1 AND DATE(r.cancelDate) BETWEEN DATE(?2) AND DATE(?3)")
    Page<ReturnInvoice> findAllByUserCodeAndReturnDateBetween(String userCode, LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT r FROM ReturnInvoice r JOIN r.invoice i WHERE DATE(r.cancelDate) BETWEEN DATE(?1) AND DATE(?2)")
    Page<ReturnInvoice> findAllByReturnDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
