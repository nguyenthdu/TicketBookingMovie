package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.FoodDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FoodService {
    void createFood(FoodDto foodDto, MultipartFile file) throws IOException;

    FoodDto getFoodById(Long id);

    void updateFood(FoodDto foodDto, MultipartFile multipartFile) throws IOException;

    void deleteFoodById(Long id);

    List<FoodDto> getAllFood(Integer page, Integer size, String code, String name, Long categoryId, String sizeFood);

    long countAllFood(String code, String name, Long categoryId, String sizeFood);
}
