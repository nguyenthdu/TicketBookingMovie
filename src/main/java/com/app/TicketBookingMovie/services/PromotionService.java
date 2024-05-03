package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.models.Promotion;

import java.time.LocalDate;
import java.util.List;

public interface PromotionService {
    void createPromotion(PromotionDto promotionDto);

    Promotion findPromotionById(Long id);

    PromotionDto getPromotionById(Long id);



    void deletePromotion(Long id);

    void updatePromotion(PromotionDto promotionDto);

    List<PromotionDto> getAllPromotion(Integer page, Integer size, LocalDate startDate,LocalDate endDate, boolean status);

    long countAllPromotion(LocalDate startDate, LocalDate endDate, boolean status);
}
