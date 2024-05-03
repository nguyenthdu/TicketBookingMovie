package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ReturnInvoiceRepository extends JpaRepository<ReturnInvoice, Integer> {
    ReturnInvoice findByInvoiceId(Long invoiceId);


    long countByCode(String code);

    @Query("SELECT COUNT(r) FROM ReturnInvoice r JOIN r.invoice i JOIN i.user u WHERE u.code = :userCode")
    long countByUserCode(String userCode);

    long countByCancelDateBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);

}
