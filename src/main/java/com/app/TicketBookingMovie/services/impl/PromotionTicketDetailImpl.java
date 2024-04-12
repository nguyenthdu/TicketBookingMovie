package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionTicketDetailDto;
import com.app.TicketBookingMovie.models.PromotionTicketDetail;
import com.app.TicketBookingMovie.repository.PromotionTicketDetailRepository;
import com.app.TicketBookingMovie.services.PromotionTicketDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PromotionTicketDetailImpl  implements PromotionTicketDetailService {
    private final PromotionTicketDetailRepository promotionTicketDetailRepository;
    private final ModelMapper modelMapper;

    public PromotionTicketDetailImpl(PromotionTicketDetailRepository promotionTicketDetailRepository, ModelMapper modelMapper) {
        this.promotionTicketDetailRepository = promotionTicketDetailRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PromotionTicketDetail createPromotionTicketDetail(PromotionTicketDetailDto promotionTicketDetailDto) {
        PromotionTicketDetail promotionTicketDetail = modelMapper.map(promotionTicketDetailDto, PromotionTicketDetail.class);
        return promotionTicketDetailRepository.save(promotionTicketDetail);
    }
}
