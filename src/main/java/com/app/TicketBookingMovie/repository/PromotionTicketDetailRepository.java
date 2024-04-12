package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionTicketDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionTicketDetailRepository extends JpaRepository<PromotionTicketDetail, Long> {
}
