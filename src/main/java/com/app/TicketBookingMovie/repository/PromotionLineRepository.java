package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionLineRepository extends JpaRepository<PromotionLine, Long> {
}
