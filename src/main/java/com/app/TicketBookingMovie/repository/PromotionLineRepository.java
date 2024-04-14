package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionLineRepository extends JpaRepository<PromotionLine, Long> {
    boolean existsByStartDateAndEndDateAndTypePromotionAndPromotion(LocalDateTime startDate, LocalDateTime endDate, ETypePromotion typePromotion, Promotion promotion);
    @Query("SELECT p FROM PromotionLine p WHERE ?1 BETWEEN p.startDate AND p.endDate AND p.status = true")
    List<PromotionLine> findActivePromotionLines(LocalDateTime currentTime);

    Page<PromotionLine> findAllByPromotionIdAndCode(Long promotionId, String promotionLineCode, Pageable pageable);

    Page<PromotionLine> findAllByPromotionIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(Long promotionId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<PromotionLine> findAllByPromotionIdAndTypePromotion(Long promotionId, ETypePromotion eTypePromotion, Pageable pageable);

    Page<PromotionLine> findAllByPromotionId(Long promotionId, Pageable pageable);
}
