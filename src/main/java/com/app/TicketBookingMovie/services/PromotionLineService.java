package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface PromotionLineService {
    void createPromotionLine(PromotionLineDto promotionLineDto, MultipartFile multipartFile)throws IOException;
    PromotionLineDto getPromotionLineById(Long promotionLineId);
    List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion);
    void deletePromotionLine(Long promotionLineId);
}
