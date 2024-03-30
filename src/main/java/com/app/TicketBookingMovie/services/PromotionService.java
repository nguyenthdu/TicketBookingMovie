package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.models.Promotion;

public interface PromotionService {
    void createPromotion(PromotionDto promotionDto);
    Promotion getPromotionById(Long id);


}
