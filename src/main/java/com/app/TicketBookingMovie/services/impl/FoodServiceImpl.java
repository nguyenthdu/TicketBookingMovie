package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.CategoryFood;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.enums.ESize;
import com.app.TicketBookingMovie.repository.FoodRepository;
import com.app.TicketBookingMovie.services.AwsService;
import com.app.TicketBookingMovie.services.CategoryFoodService;
import com.app.TicketBookingMovie.services.FoodService;
import com.app.TicketBookingMovie.services.PriceDetailService;
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
public class FoodServiceImpl implements FoodService {
    private final ModelMapper modelMapper;
    private final FoodRepository foodRepository;
    private final CategoryFoodService categoryFoodService;
    private final PriceDetailService priceDetailService;
    private final AwsService awsService;


    public FoodServiceImpl(ModelMapper modelMapper, FoodRepository foodRepository, PriceDetailService priceDetailService, CategoryFoodService categoryFoodService, AwsService awsService) {
        this.modelMapper = modelMapper;
        this.foodRepository = foodRepository;
        this.priceDetailService = priceDetailService;
        this.categoryFoodService = categoryFoodService;
        this.awsService = awsService;
    }

    public String randomCode() {
        return "DA" + LocalDateTime.now().getNano();
    }


    @Override
    public void createFood(FoodDto foodDto) {
        if (foodRepository.findByName(foodDto.getName()).isPresent()) {
            throw new AppException("name: " + foodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        Food food = new Food();
        getSize(foodDto, food);
        food.setCode(randomCode());
        food.setName(foodDto.getName());
        food.setStatus(foodDto.isStatus());
        food.setImage(foodDto.getImage());
        food.setQuantity(foodDto.getQuantity());
        food.setCreatedDate(LocalDateTime.now());
        food.setCategoryFood(categoryFoodService.findCategoryFoodById(foodDto.getCategoryId()));
        food.setPrice(priceDetailService.createPriceDetail(foodDto.getPriceDetail()));
        food.setCreatedDate(LocalDateTime.now());
        foodRepository.save(food);
    }

    private void getSize(FoodDto foodDto, Food food) {
        switch (foodDto.getSize()) {
            case "SMALL":
                food.setSize(ESize.SMALL);
                break;
            case "MEDIUM":
                food.setSize(ESize.MEDIUM);
                break;
            case "LARGE":
                food.setSize(ESize.LARGE);
                break;
        }
    }

    @Override
    public FoodDto getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Food not found with id: " + id, HttpStatus.NOT_FOUND));
        //xử lý in ra tên category
        FoodDto foodDto = modelMapper.map(food, FoodDto.class);
        foodDto.setCategoryName(food.getCategoryFood().getName());
        return foodDto;


    }

    @Override
    public Food findById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Food not found with id: " + id, HttpStatus.NOT_FOUND));

    }

//    private String getFileExtension(String fileName) {
//        return fileName.substring(fileName.lastIndexOf("."));
//    }

    @Override
    public void updateFood(FoodDto foodDto) {
        Food food = foodRepository.findById(foodDto.getId())
                .orElseThrow(() -> new AppException("Food not found with id: " + foodDto.getId(), HttpStatus.NOT_FOUND));
        if (!foodDto.getName().isEmpty() && !foodDto.getName().isBlank() && !foodDto.getName().equalsIgnoreCase(food.getName())) {
            if (foodRepository.findByName(foodDto.getName()).isPresent()) {
                throw new AppException("name: " + foodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
            }
            food.setName(foodDto.getName());
        } else {
            food.setName(food.getName());
        }
        if (!foodDto.getSize().isEmpty() && !foodDto.getSize().isBlank()) {
            getSize(foodDto, food);
        } else {
            food.setSize(food.getSize());
        }
        if (foodDto.isStatus() != food.isStatus()) {
            food.setStatus(foodDto.isStatus());
        } else {
            food.setStatus(food.isStatus());
        }

        if (foodDto.getCategoryId() > 0) {
            CategoryFood categoryFood = categoryFoodService.findCategoryFoodById(foodDto.getCategoryId());
            food.setCategoryFood(categoryFood);
        } else {
            food.setCategoryFood(food.getCategoryFood());
        }
        if (foodDto.getQuantity() >= 0) {
            food.setQuantity(foodDto.getQuantity());
        } else {
            food.setQuantity(food.getQuantity());
        }
        if (!foodDto.getImage().isEmpty() && !foodDto.getImage().isBlank() && !foodDto.getImage().equalsIgnoreCase(food.getImage())) {
            awsService.deleteImage(food.getImage());
            food.setImage(foodDto.getImage());
        } else {
            food.setImage(food.getImage());
        }

        if (foodDto.getPriceDetail().getPrice() > 0 && foodDto.getPriceDetail().getPrice() != food.getPrice().getPrice()) {
            food.getPrice().setPrice(foodDto.getPriceDetail().getPrice());
        } else {
           food.getPrice().setPrice(food.getPrice().getPrice());
        }
        if(foodDto.getPriceDetail().isStatus() != food.getPrice().isStatus()) {
            food.getPrice().setStatus(foodDto.getPriceDetail().isStatus());
        }else {
            food.getPrice().setStatus(food.getPrice().isStatus());
        }
        foodRepository.save(food);
        modelMapper.map(food, FoodDto.class);

    }

    @Override
    public void deleteFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Food not found with id: " + id, HttpStatus.NOT_FOUND));
        awsService.deleteImage(food.getImage());
        foodRepository.deleteById(id);
    }


    @Override
    public List<FoodDto> getAllFood(Integer page, Integer size, String code, String name, Long categoryId, String sizeFood) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Food> food;
        if (code != null && !code.isEmpty()) {
            food = foodRepository.findAllByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            food = foodRepository.findAllByNameContaining(name, pageable);
        } else if (categoryId != null) {
            food = foodRepository.findAllByCategoryFoodId(categoryId, pageable);
        } else if (sizeFood != null && !sizeFood.isEmpty()) {
            food = foodRepository.findAllBySize(ESize.valueOf(sizeFood), pageable);
        } else {
            food = foodRepository.findAll(pageable);
        }

        //in ra category name
        return food.stream().sorted(Comparator.comparing(Food::getCreatedDate).reversed())
                .map(f -> {

                    FoodDto foodDto = modelMapper.map(f, FoodDto.class);
                    foodDto.setCategoryName(f.getCategoryFood().getName());
                    return foodDto;
                }).toList();


    }

    @Override
    public long countAllFood(String code, String name, Long categoryId, String sizeFood) {
        if (code != null && !code.isEmpty()) {
            return foodRepository.countAllByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return foodRepository.countAllByNameContaining(name);
        } else if (categoryId != null) {
            return foodRepository.countAllByCategoryFoodId(categoryId);
        } else if (sizeFood != null && !sizeFood.isEmpty()) {
            return foodRepository.countAllBySize(ESize.valueOf(sizeFood));
        } else {
            return foodRepository.count();
        }
    }
}
