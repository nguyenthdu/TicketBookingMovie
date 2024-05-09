package com.app.TicketBookingMovie.controller;

import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PageResponse;
import com.app.TicketBookingMovie.payload.response.MessageResponse;
import com.app.TicketBookingMovie.services.FoodService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/food")
public class FoodController {
    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    public ResponseEntity<MessageResponse> createFood(
            @RequestParam("name") String name,
            @RequestParam("image") String image,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cinemaIds") Long cinemaIds,
            @RequestParam("sizeFood") String sizeFood,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") boolean status) {

        FoodDto foodDto = new FoodDto();
        foodDto.setImage(image);
        foodDto.setName(name);
        foodDto.setCategoryId(categoryId);
        foodDto.setCinemaId(cinemaIds);
        foodDto.setSize(sizeFood);
        foodDto.setQuantity(quantity);
        foodDto.setStatus(status);
        try {
            foodService.createFood(foodDto);
            return ResponseEntity.ok(new MessageResponse("Thêm thành công" + name, HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodDto> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @PutMapping
    public ResponseEntity<MessageResponse> updateFood(
            @RequestParam Long id,
            @RequestParam("name") String name,
            @RequestParam("image") String image,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam("cinemaIds") Long cinemaIds,
            @RequestParam("sizeFood") String sizeFood,
            @RequestParam("quantity") int quantity,
            @RequestParam("status") boolean status) {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(id);
        foodDto.setImage(image);
        foodDto.setName(name);
        foodDto.setCategoryId(categoryId);
        foodDto.setCinemaId(cinemaIds);
        foodDto.setSize(sizeFood);
        foodDto.setQuantity(quantity);
        foodDto.setStatus(status);

        try {
            foodService.updateFood(foodDto);
            return ResponseEntity.ok(new MessageResponse("Cập nhật thành công " + foodDto.getName(), HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteFood(@PathVariable Long id) {
        try {
            foodService.deleteFoodById(id);
            return ResponseEntity.ok(new MessageResponse("Xóa thành công!!", HttpStatus.OK.value(), Instant.now().toString()));
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage(), e.getStatus(), e.getTimestamp()));
        }
    }

    @GetMapping
    public ResponseEntity<PageResponse<FoodDto>> getAllFood(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "cinemaId", required = false) Long cinemaId,
            @RequestParam(value = "sizeFood", required = false) String sizeFood) {
        PageResponse<FoodDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(foodService.getAllFood(page, size,cinemaId, code, name, categoryId, sizeFood));
        pageResponse.setTotalElements(foodService.countAllFood(cinemaId,code, name, categoryId, sizeFood));
        pageResponse.setTotalPages((int) Math.ceil((double) pageResponse.getTotalElements() / size));
        pageResponse.setCurrentPage(page);
        pageResponse.setPageSize(size);
        return ResponseEntity.ok(pageResponse);
    }
}
