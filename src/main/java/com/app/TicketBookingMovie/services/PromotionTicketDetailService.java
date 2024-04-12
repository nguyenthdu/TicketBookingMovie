package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PromotionTicketDetailDto;
import com.app.TicketBookingMovie.models.PromotionTicketDetail;

public interface PromotionTicketDetailService {
    PromotionTicketDetail createPromotionTicketDetail(PromotionTicketDetailDto promotionTicketDetailDto);

}
