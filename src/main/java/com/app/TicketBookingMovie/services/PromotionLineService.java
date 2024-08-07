package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.models.PromotionLine;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionLineService {
    @Transactional
    void createPromotionLine(PromotionLineDto promotionLineDto);
    void updatePromotionLine(PromotionLineDto promotionLineDto);
    PromotionLineDto getPromotionLineById(Long promotionLineId);
    PromotionLine findById(Long promotionLineId);
    List<PromotionLine> getPromotionLineByInvoiceId(Long invoiceId);
    List<PromotionLine> getPromotionLineActive();
    PromotionLineDto showPromotionLineDiscountMatchInvoice(BigDecimal totalPrice);
    PromotionLineDto showPromotionLineFoodMatchInvoice(List<Long> foodId,Long cinemaId);
    PromotionLineDto showPromotionLineTicketMatchInvoice(List<Long> seatId,Long showTimeId);
    List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String typePromotion);
    long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate , String typePromotion);
    void deletePromotionLine(Long promotionLineId);
    void updateQuantityPromotionLine(Long promotionLineId, int quantity);
}
