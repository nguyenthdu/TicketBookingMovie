package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.CategoryFoodDto;
import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.CategoryFoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("api/categoryFood")
public class CategoryFoodController {
    private final CategoryFoodService categoryFoodService;

    public CategoryFoodController(CategoryFoodService categoryFoodService) {
        this.categoryFoodService = categoryFoodService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createCategoryFood(
            @RequestParam("name") String name) {
        CategoryFoodDto categoryFoodDto = new CategoryFoodDto();
        categoryFoodDto.setName(name);
        try {
            categoryFoodService.createCategoryFood(categoryFoodDto);
            return ResponseEntity.ok(new MessageResponseDto("Category Food created successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryFoodDto> getCategoryFood(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(categoryFoodService.getCategoryFoodById(id));
        } catch (AppException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping
    public ResponseEntity<MessageResponseDto> updateCategoryFood(
            @RequestParam("id") Long id,
            @RequestParam("name") String name) {
        CategoryFoodDto categoryFoodDto = new CategoryFoodDto();
        categoryFoodDto.setId(id);
        categoryFoodDto.setName(name);
        try {
            categoryFoodService.updateCategoryFood(categoryFoodDto);
            return ResponseEntity.ok(new MessageResponseDto("Category Food updated successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteCategoryFood(@PathVariable Long id) {
        try {
            categoryFoodService.deleteCategoryFoodById(id);
            return ResponseEntity.ok(new MessageResponseDto("Category Food deleted successfully", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<CategoryFoodDto>> getAllCategoryFood(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "code") String code,
            @RequestParam(required = false, name = "name") String name) {
        PageResponse<CategoryFoodDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(categoryFoodService.getAllCategoryFood(page, size, code, name));
        pageResponse.setTotalElements(categoryFoodService.countAllCategoryFood(code, name));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);

    }

}

