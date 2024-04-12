package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionFoodDetailDto;
import com.app.TicketBookingMovie.models.PromotionFoodDetail;
import com.app.TicketBookingMovie.repository.PromotionFoodDetailRepository;
import com.app.TicketBookingMovie.services.PromotionFoodDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PromotionFoodDetailImpl implements PromotionFoodDetailService {
    private final PromotionFoodDetailRepository promotionFoodDetailRepository;
    private final ModelMapper modelMapper;

    public PromotionFoodDetailImpl(PromotionFoodDetailRepository promotionFoodDetailRepository, ModelMapper modelMapper) {
        this.promotionFoodDetailRepository = promotionFoodDetailRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public PromotionFoodDetail createPromotionFoodDetail(PromotionFoodDetailDto promotionFoodDetailDto) {
        PromotionFoodDetail promotionFoodDetail = modelMapper.map(promotionFoodDetailDto, PromotionFoodDetail.class);
        return promotionFoodDetailRepository.save(promotionFoodDetail);
    }
}
