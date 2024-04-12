package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionFoodDetailDto;
import com.app.TicketBookingMovie.models.PromotionFoodDetail;

public interface PromotionFoodDetailService {
    PromotionFoodDetail createPromotionFoodDetail(PromotionFoodDetailDto promotionFoodDetailDto);
}
