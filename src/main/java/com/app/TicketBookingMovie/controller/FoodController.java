package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.dtos.MessageResponseDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.services.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/food")
public class FoodController {
    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createFood(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image,
            @RequestParam("price") double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("sizeFood") String sizeFood,
            @RequestParam("status") boolean status) throws IOException {
        FoodDto foodDto = new FoodDto();
        foodDto.setName(name);
        foodDto.setPrice(price);
        foodDto.setCategoryId(categoryId);
        foodDto.setSize(sizeFood);
        foodDto.setStatus(status);
        try {
            foodService.createFood(foodDto, image);
            return ResponseEntity.ok(new MessageResponseDto("Create food success with name: " + name, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<FoodDto> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }
    @PutMapping
    public ResponseEntity<MessageResponseDto> updateFood(
            @RequestParam Long id,
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image,
            @RequestParam("price") double price,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("sizeFood") String sizeFood,
            @RequestParam("status") boolean status) throws IOException {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(id);
        foodDto.setName(name);
        foodDto.setPrice(price);
        foodDto.setCategoryId(categoryId);
        foodDto.setSize(sizeFood);
        foodDto.setStatus(status);
        try {
            foodService.updateFood(foodDto, image);
            return ResponseEntity.ok(new MessageResponseDto("Update food success with name: " + name, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDto> deleteFood(@PathVariable Long id) {
        try {
            foodService.deleteFoodById(id);
            return ResponseEntity.ok(new MessageResponseDto("Delete food success with id: " + id, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponseDto(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }
    @GetMapping
    public ResponseEntity<PageResponse<FoodDto>> getAllFood(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue ="10") Integer size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "sizeFood", required = false) String sizeFood) {
        PageResponse<FoodDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(foodService.getAllFood(page, size, code, name,categoryId, sizeFood));
        pageResponse.setTotalElements(foodService.countAllFood(code, name, categoryId,sizeFood));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}
