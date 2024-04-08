package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.models.Food;

import java.util.List;

public interface FoodService {
    void createFood(FoodDto foodDto);

    FoodDto getFoodById(Long id);
    Food findById(Long id);

    void updateFood(FoodDto foodDto);

    void deleteFoodById(Long id);

    List<FoodDto> getAllFood(Integer page, Integer size, String code, String name, Long categoryId, String sizeFood);

    long countAllFood(String code, String name, Long categoryId, String sizeFood);
}
