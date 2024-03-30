package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.PromotionDetail;
import com.app.TicketBookingMovie.repository.PromotionDetailRepository;
import com.app.TicketBookingMovie.services.FoodService;
import com.app.TicketBookingMovie.services.PromotionDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class PromotionDetailServiceImpl implements PromotionDetailService {
    private final PromotionDetailRepository promotionDetailRepository;
    private final ModelMapper modelMapper;
    private final FoodService foodService;


    public PromotionDetailServiceImpl(PromotionDetailRepository promotionDetailRepository, ModelMapper modelMapper, FoodService foodService) {
        this.promotionDetailRepository = promotionDetailRepository;
        this.modelMapper = modelMapper;
        this.foodService = foodService;
    }

    @Override
    public PromotionDetail createPromotionDetailGift(PromotionDetailDto promotionDetailDto) {
        PromotionDetail promotionDetail = modelMapper.map(promotionDetailDto, PromotionDetail.class);
        Food food = foodService.findById(promotionDetailDto.getFoodId());
        promotionDetail.setFood(food);
        promotionDetail.setDiscountValue(0);
        return promotionDetailRepository.save(promotionDetail);
    }

    @Override
    public PromotionDetail createPromotionDetailDiscount(PromotionDetailDto promotionDetailDto) {
        PromotionDetail promotionDetail = modelMapper.map(promotionDetailDto, PromotionDetail.class);
        promotionDetail.setFood(null);
        return promotionDetailRepository.save(promotionDetail);
    }
}
