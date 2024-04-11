package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionDiscountDetailDto;
import com.app.TicketBookingMovie.models.PromotionDiscountDetail;

public interface PromotionDiscountDetailService {
    PromotionDiscountDetail createPromotionDiscountDetail(PromotionDiscountDetailDto promotionDiscountDetailDto);
}
