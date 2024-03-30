package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.repository.PromotionRepository;
import com.app.TicketBookingMovie.services.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {
    private  final PromotionRepository promotionRepository;
    private final ModelMapper modelMapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository, ModelMapper modelMapper) {
        this.promotionRepository = promotionRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createPromotion(PromotionDto promotionDto) {
        Promotion promotion = modelMapper.map(promotionDto, Promotion.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = promotionDto.getStartDate();
        LocalDateTime endDate = promotionDto.getEndDate();

        // Check if the start date is at least one day after the current date
        if (!startDate.isAfter(now.plusDays(1))) {
            throw new AppException("The start date must be at least one day after the current date", HttpStatus.BAD_REQUEST);
        }

        // Check if the end date is after the start date
        if (!endDate.isAfter(startDate)) {
            throw new AppException("The end date must be after the start date", HttpStatus.BAD_REQUEST);
        }

        // Check if the promotion period overlaps with any existing promotions
        List<Promotion> existingPromotions = promotionRepository.findAll();
        for (Promotion existingPromotion : existingPromotions) {
            if ((startDate.isBefore(existingPromotion.getEndDate()) || startDate.isEqual(existingPromotion.getEndDate())) &&
                    (endDate.isAfter(existingPromotion.getStartDate()) || endDate.isEqual(existingPromotion.getStartDate()))) {
                throw new AppException("The promotion period overlaps with an existing promotion", HttpStatus.BAD_REQUEST);
            }
        }


        promotionRepository.save(promotion);
    }

    @Override
    public Promotion getPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() -> new AppException("Promotion not found", HttpStatus.NOT_FOUND));
    }

}
