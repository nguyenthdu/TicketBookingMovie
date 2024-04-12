package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
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
//    boolean existsByPromotionDetail_Food_Id(Long foodId);
//
//    PromotionLine findByCode(String promotionLineCode);
//
//    Page<PromotionLine> findAllByPromotionId(Long promotionId, Pageable pageable);
//
//    Page<PromotionLine> findAllByCode(String promotionLineCode, Pageable pageable);
//
//    Page<PromotionLine> findAllByApplicableObject(EApplicableObject applicableObject, Pageable pageable);
//
//    Page<PromotionLine> findAllByTypePromotion(ETypePromotion typePromotion, Pageable pageable);
//
//    Page<PromotionLine> findAllByStartDateAndEndDate(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
//
//
//    long countByPromotionId(Long promotionId);
//
//    long countByCode(String promotionLineCode);
//
//    long countAllByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime startDate, LocalDateTime endDate);
//
//    long countAllByApplicableObject(EApplicableObject eApplicableObject);
//
//    long countAllByTypePromotion(ETypePromotion eTypePromotion);
//


}
