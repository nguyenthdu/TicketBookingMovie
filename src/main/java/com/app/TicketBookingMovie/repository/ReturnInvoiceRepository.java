package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ReturnInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnInvoiceRepository extends JpaRepository<ReturnInvoice, Integer>{
}
