package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionLineService {
    @Transactional
    void createPromotionLine(PromotionLineDto promotionLineDto);

    PromotionLineDto getPromotionLineById(Long promotionLineId);
    List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    void deletePromotionLine(Long promotionLineId);
}
