package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.CategoryFood;
import com.app.TicketBookingMovie.models.Cinema;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.enums.ESize;
import com.app.TicketBookingMovie.repository.FoodRepository;
import com.app.TicketBookingMovie.services.AwsService;
import com.app.TicketBookingMovie.services.CategoryFoodService;
import com.app.TicketBookingMovie.services.CinemaService;
import com.app.TicketBookingMovie.services.FoodService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FoodServiceImpl implements FoodService {
    private final ModelMapper modelMapper;
    private final FoodRepository foodRepository;
    private final CategoryFoodService categoryFoodService;
    private final AwsService awsService;
    private final CinemaService cinemaService;

    public FoodServiceImpl(ModelMapper modelMapper,
            FoodRepository foodRepository,
            CategoryFoodService categoryFoodService,
            AwsService awsService,
            CinemaService cinemaService) {
        this.modelMapper = modelMapper;
        this.foodRepository = foodRepository;
        this.categoryFoodService = categoryFoodService;
        this.awsService = awsService;
        this.cinemaService = cinemaService;
    }

    public String randomCode() {
        return "DA" + LocalDateTime.now().getNano();
    }

    @Override
    public void createFood(FoodDto foodDto) {
        // nếu tên food trong 1 cinema đã tồn tại
        Cinema cinema = cinemaService.findById(foodDto.getCinemaId());
        if (foodRepository.findByNameAndCinemaId(foodDto.getName(), foodDto.getCinemaId()).isPresent()) {
            throw new AppException("Tên " + foodDto.getName() + "đã tồn tại trong rạp " + cinema.getName() + "!!!",
                    HttpStatus.BAD_REQUEST);
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
        // lấy danh sách rạp
        if (!cinema.isStatus() && !food.isStatus()) {
            throw new AppException(
                    "Không thể đặt trạng thái của đồ ăn hoạt động khi trạng thái của rạp không hoạt động!!!",
                    HttpStatus.BAD_REQUEST);
        }
        food.setCinema(cinemaService.findById(foodDto.getCinemaId()));
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

    // //get price food
    // public BigDecimal getPriceFood(Food food) {
    // List<PriceDetail> currentPriceDetails = priceDetailService.priceActive();
    // Optional<PriceDetail> foodPriceDetailOptional = currentPriceDetails.stream()
    // .filter(detail -> detail.getType() == EDetailType.FOOD &&
    // Objects.equals(detail.getFood().getId(), food.getId()))
    // .findFirst();
    // return
    // foodPriceDetailOptional.map(PriceDetail::getPrice).orElse(BigDecimal.ZERO);
    // }

    @Override
    public FoodDto getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm đồ ăn với id: : " + id, HttpStatus.NOT_FOUND));
        // xử lý in ra tên category

        return convertFoodDto(food);

    }

    @Override
    public Food findById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm đồ ăn với id: " + id, HttpStatus.NOT_FOUND));

    }

    @Override
    public void updateFood(FoodDto foodDto) {
        Cinema cinema = cinemaService.findById(foodDto.getCinemaId());
        Food food = foodRepository.findById(foodDto.getId())
                .orElseThrow(
                        () -> new AppException("Không tìm đồ ăn với id: " + foodDto.getId(), HttpStatus.NOT_FOUND));
        if (!foodDto.getName().isEmpty() && !foodDto.getName().isBlank()
                && !foodDto.getName().equalsIgnoreCase(food.getName())) {
            if (foodRepository.findByNameAndCinemaId(foodDto.getName(), foodDto.getCinemaId()).isPresent()) {
                throw new AppException("Tên " + foodDto.getName() + "đã tồn tại trong rạp " + cinema.getName(),
                        HttpStatus.BAD_REQUEST);
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
        if (!foodDto.getImage().isEmpty() && !foodDto.getImage().isBlank()
                && !foodDto.getImage().equalsIgnoreCase(food.getImage())) {
            awsService.deleteImage(food.getImage());
            food.setImage(foodDto.getImage());
        } else {
            food.setImage(food.getImage());
        }
        food.setCinema(cinemaService.findById(foodDto.getCinemaId()));
        food.setCreatedDate(LocalDateTime.now());
        foodRepository.save(food);

    }

    @Override
    public void updateQuantityFood(Long foodId, Long cinemaId, int quantity) {
        // tìm đồ ăn trong rạp
        Food food = foodRepository.findByIdAndCinemaId(foodId, cinemaId)
                .orElseThrow(() -> new AppException("Không tìm thấy đồ ăn với id: " + foodId + " và rạp: " + cinemaId,
                        HttpStatus.NOT_FOUND));
        food.setQuantity(food.getQuantity() + quantity);
        foodRepository.save(food);
    }

    @Override
    public void deleteFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm đồ ăn với id: " + id, HttpStatus.NOT_FOUND));
        awsService.deleteImage(food.getImage());
        foodRepository.deleteById(id);
    }

    @Override
    public List<FoodDto> getAllFood(Integer page, Integer size, Long cinemaId, String code, String name,
            Long categoryId, String sizeFood) {

        List<Food> pageFood = foodRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if (cinemaId != null) {
            if (code != null && !code.isEmpty()) {
                pageFood = pageFood.stream()
                        .filter(food -> food.getCinema().getId().equals(cinemaId) && food.getCode().contains(code))
                        .toList();
            } else if (name != null && !name.isEmpty()) {
                pageFood = pageFood.stream().filter(food -> food.getCinema().getId().equals(cinemaId)
                        && food.getName().toLowerCase().contains(name.toLowerCase())).toList();
            } else if (categoryId != null) {
                pageFood = pageFood.stream().filter(food -> food.getCinema().getId().equals(cinemaId)
                        && food.getCategoryFood().getId().equals(categoryId)).toList();
            } else if (sizeFood != null && !sizeFood.isEmpty()) {
                pageFood = pageFood.stream().filter(
                        food -> food.getCinema().getId().equals(cinemaId) && food.getSize().name().equals(sizeFood))
                        .toList();
            } else {
                pageFood = pageFood.stream().filter(food -> food.getCinema().getId().equals(cinemaId)).toList();
            }
        } else {
            pageFood = pageFood.stream().toList();
        }

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, pageFood.size());
        return pageFood.subList(fromIndex, toIndex).stream().map(this::convertFoodDto).toList();
    }

    private FoodDto convertFoodDto(Food f) {
        FoodDto foodDto = new FoodDto();
        foodDto.setId(f.getId());
        foodDto.setCode(f.getCode());
        foodDto.setName(f.getName());
        foodDto.setImage(f.getImage());
        foodDto.setQuantity(f.getQuantity());
        foodDto.setSize(f.getSize().name());
        foodDto.setCategoryId(f.getCategoryFood().getId());
        foodDto.setCategoryName(f.getCategoryFood().getName());
        foodDto.setCinemaId(f.getCinema().getId());
        foodDto.setStatus(f.isStatus());
        foodDto.setCreatedDate(f.getCreatedDate());
        f.getPriceDetails().stream().filter(PriceDetail::isStatus).findFirst().ifPresent(priceDetail -> {
            foodDto.setPrice(priceDetail.getPrice());
            foodDto.setActive_price(priceDetail.isStatus());
        });
        return foodDto;
    }

    @Override
    public long countAllFood(Long cinemaId, String code, String name, Long categoryId, String sizeFood) {
        if (cinemaId != null) {
            if (code != null && !code.isEmpty()) {
                return foodRepository.countByCinemaIdAndCodeContaining(cinemaId, code);
            } else if (name != null && !name.isEmpty()) {
                return foodRepository.countByCinemaIdAndNameContaining(cinemaId, name);
            } else if (categoryId != null) {
                return foodRepository.countByCinemaIdAndCategoryFoodId(cinemaId, categoryId);
            } else if (sizeFood != null && !sizeFood.isEmpty()) {
                return foodRepository.countByCinemaIdAndSize(cinemaId, ESize.valueOf(sizeFood));
            } else {
                return foodRepository.countByCinemaId(cinemaId);
            }
        } else {
            return foodRepository.count();
        }
    }
}
