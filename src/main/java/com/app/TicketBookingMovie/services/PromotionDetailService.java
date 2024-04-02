package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.models.PromotionDetail;

public interface PromotionDetailService {
    PromotionDetail createPromotionDetailGift(PromotionDetailDto promotionDetailDto);

    PromotionDetail createPromotionDetailDiscount(PromotionDetailDto promotionDetailDto);

    PromotionDetailDto getPromotionDetailByPromotionLineId(Long promotionLineId);
}
