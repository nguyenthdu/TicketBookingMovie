package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.CategoryFoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.CategoryFood;
import com.app.TicketBookingMovie.repository.CateogryFoodRepository;
import com.app.TicketBookingMovie.services.CategoryFoodService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryFoodServiceImpl implements CategoryFoodService {
    private final CateogryFoodRepository cateogryFoodRepository;
    private final ModelMapper modelMapper;

    public CategoryFoodServiceImpl(CateogryFoodRepository cateogryFoodRepository, ModelMapper modelMapper) {
        this.cateogryFoodRepository = cateogryFoodRepository;
        this.modelMapper = modelMapper;
    }

    public String randomCode() {
        return "DM"+ LocalDateTime.now().getNano();
    }

    @Override
    public void createCategoryFood(CategoryFoodDto categoryFoodDto) {
        if (cateogryFoodRepository.findByName(categoryFoodDto.getName()).isPresent()) {
            throw new AppException("name: " + categoryFoodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        CategoryFood categoryFood = modelMapper.map(categoryFoodDto, CategoryFood.class);
        categoryFood.setCode(randomCode());
        categoryFood.setCreatedDate(LocalDateTime.now());
        cateogryFoodRepository.save(categoryFood);
        modelMapper.map(categoryFood, CategoryFoodDto.class);

    }

    @Override
    public CategoryFoodDto getCategoryFoodById(Long id) {
       CategoryFood categoryFood = cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Category not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(categoryFood, CategoryFoodDto.class);

    }

    @Override
    public CategoryFood findCategoryFoodById(Long id) {
        return cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Category not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void updateCategoryFood(CategoryFoodDto categoryFoodDto) {
        CategoryFood categoryFood = cateogryFoodRepository.findById(categoryFoodDto.getId()).orElseThrow(() -> new AppException("Category not found with id: " + categoryFoodDto.getId(), HttpStatus.NOT_FOUND));

        if(!categoryFoodDto.getName().isEmpty() && !categoryFoodDto.getName().isBlank()) {
            if (cateogryFoodRepository.findByName(categoryFoodDto.getName()).isPresent()) {
                throw new AppException("name: " + categoryFoodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
            }
            categoryFood.setName(categoryFoodDto.getName());
        }else{
            categoryFood.setName(categoryFood.getName());
        }
        cateogryFoodRepository.save(categoryFood);
        modelMapper.map(categoryFood, CategoryFoodDto.class);

    }

    @Override
    public void deleteCategoryFoodById(Long id) {
        CategoryFood categoryFood = cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Category not found with id: " + id, HttpStatus.NOT_FOUND));
        cateogryFoodRepository.delete(categoryFood);
    }

    @Override
    public List<CategoryFoodDto> getAllCategoryFood(int page, int size,String code, String name) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryFood> pageCategory;
        if(code!=null && !code.isEmpty()){
            pageCategory = cateogryFoodRepository.findByCodeContaining(code, pageable);
        }
       else if (name != null && !name.isEmpty()) {
            pageCategory = cateogryFoodRepository.findByNameContaining(name, pageable);
        } else {
            pageCategory = cateogryFoodRepository.findAll(pageable);
        }
        return pageCategory.stream().sorted(Comparator.comparing(CategoryFood::getCreatedDate).reversed())
                .map(categoryFood -> modelMapper.map(categoryFood, CategoryFoodDto.class))
                .toList();
    }

    @Override
    public long countAllCategoryFood(String code, String name) {
        if(code!=null && !code.isEmpty()){
            return cateogryFoodRepository.countByCodeContaining(code);
        }
        else if (name != null && !name.isEmpty()) {
            return cateogryFoodRepository.countByNameContaining(name);
        }
        return cateogryFoodRepository.count();
    }


}
