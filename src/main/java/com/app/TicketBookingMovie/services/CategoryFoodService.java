package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.CategoryFoodDto;

import java.util.List;

public interface CategoryFoodService {
    void createCategoryFood(CategoryFoodDto categoryFoodDto);

    CategoryFoodDto getCategoryFoodById(Long id);

    void updateCategoryFood(CategoryFoodDto categoryFoodDto);


    void deleteCategoryFoodById(Long id);
    List<CategoryFoodDto> getAllCategoryFood(int page, int size,String code, String name);
    long countAllCategoryFood(String code, String name);
}
