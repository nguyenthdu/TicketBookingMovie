//package com.app.TicketBookingMovie.services;
//
//import com.app.TicketBookingMovie.dtos.PromotionDto;
//import com.app.TicketBookingMovie.models.Promotion;
//import com.app.TicketBookingMovie.models.PromotionLine;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//
//public interface PromotionService {
//    void createPromotion(PromotionDto promotionDto);
//
//    Promotion findPromotionById(Long id);
//
//    PromotionDto getPromotionById(Long id);
//
//    List<PromotionLine> getAllPromotionFitBill(BigDecimal totalValueBill, String applicableObject);
//
////    PromotionLine getPromotionLineByCodeAndFitBill(String promotionLineCode, double totalValueBill, LocalDateTime dateTime, String applicableObject);
//
//    void deletePromotion(Long id);
//
//    void updatePromotion(PromotionDto promotionDto);
//
//    List<PromotionDto> getAllPromotion(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate, boolean status);
//
//    long countAllPromotion(LocalDateTime startDate, LocalDateTime endDate, boolean status);
//}
