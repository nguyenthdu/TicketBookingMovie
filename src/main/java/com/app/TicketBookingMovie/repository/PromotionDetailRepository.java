package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long>{
}
