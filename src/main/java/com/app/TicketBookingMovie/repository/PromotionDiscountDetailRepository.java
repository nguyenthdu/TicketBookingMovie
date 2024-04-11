package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionDiscountDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionDiscountDetailRepository  extends JpaRepository<PromotionDiscountDetail, Long> {
}
