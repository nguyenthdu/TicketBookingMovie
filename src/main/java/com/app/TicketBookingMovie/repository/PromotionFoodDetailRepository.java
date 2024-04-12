package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionFoodDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionFoodDetailRepository  extends JpaRepository<PromotionFoodDetail, Long> {
}
