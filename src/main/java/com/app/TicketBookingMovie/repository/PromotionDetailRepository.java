package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long>{

    Optional<PromotionDetail> findByPromotionLineId(Long promotionLineId);
}
