package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDiscountDetailDto;
import com.app.TicketBookingMovie.models.PromotionDiscountDetail;
import com.app.TicketBookingMovie.repository.PromotionDiscountDetailRepository;
import com.app.TicketBookingMovie.services.PromotionDiscountDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PromotionDiscountDetailServiceImpl implements PromotionDiscountDetailService {
    private final PromotionDiscountDetailRepository promotionDiscountDetailRepository;
    private final ModelMapper modelMapper;

    public PromotionDiscountDetailServiceImpl(PromotionDiscountDetailRepository promotionDiscountDetailRepository, ModelMapper modelMapper) {
        this.promotionDiscountDetailRepository = promotionDiscountDetailRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PromotionDiscountDetail createPromotionDiscountDetail(PromotionDiscountDetailDto promotionDiscountDetailDto) {
        PromotionDiscountDetail promotionDiscountDetail = modelMapper.map(promotionDiscountDetailDto, PromotionDiscountDetail.class);
        return promotionDiscountDetailRepository.save(promotionDiscountDetail);

    }
}
