package com.app.TicketBookingMovie.services.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.app.TicketBookingMovie.dtos.FoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.enums.ESize;
import com.app.TicketBookingMovie.repository.CateogryFoodRepository;
import com.app.TicketBookingMovie.repository.FoodRepository;
import com.app.TicketBookingMovie.repository.MovieRepository;
import com.app.TicketBookingMovie.services.FoodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class FoodServiceImpl implements FoodService {
    @Value("${S3_BUCKET_NAME_FOOD}")
    private String BUCKET_NAME_FOOD;
    private final ModelMapper modelMapper;
    private final MovieRepository movieRepository;
    private final AmazonS3 amazonS3;
    private final FoodRepository foodRepository;
    private final CateogryFoodRepository cateogryFoodRepository;
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    public FoodServiceImpl(ModelMapper modelMapper, MovieRepository movieRepository, AmazonS3 amazonS3, FoodRepository foodRepository, CateogryFoodRepository cateogryFoodRepository) {
        this.modelMapper = modelMapper;
        this.movieRepository = movieRepository;
        this.amazonS3 = amazonS3;
        this.foodRepository = foodRepository;
        this.cateogryFoodRepository = cateogryFoodRepository;
    }

    public String randomCode() {
        return "DA"+LocalDateTime.now().getNano();
    }

    public void checkFileType(MultipartFile multipartFile) {
        String fileName = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = fileName.substring(fileName.lastIndexOf("."));
        if (!fileType.equals(".jpg") && !fileType.equals(".png")) {
            throw new AppException("Only .jpg and .png files are allowed", HttpStatus.BAD_REQUEST);
        }
    }

    private File convertMultiPartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

    @Override
    public void createFood(FoodDto foodDto, MultipartFile multipartFile) throws IOException {
        if (foodRepository.findByName(foodDto.getName()).isPresent()) {
            throw new AppException("name: " + foodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
        }
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new AppException("Image is required", HttpStatus.BAD_REQUEST);
        }
        String code = randomCode();
        checkFileType(multipartFile);
        if (multipartFile.getSize() > MAX_SIZE) {
            throw new AppException("File size is too large. must < 10mb", HttpStatus.BAD_REQUEST);
        }
        String image = Objects.requireNonNull(multipartFile.getOriginalFilename());
        String fileType = image.substring(image.lastIndexOf("."));
        String fileName = code + "_" + LocalDateTime.now() + fileType;
        File file = convertMultiPartFileToFile(multipartFile);
        amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_FOOD, fileName, file));
        file.delete();
        String uploadLink = amazonS3.getUrl(BUCKET_NAME_FOOD, fileName).toString();
        Food food = modelMapper.map(foodDto, Food.class);

        if (foodDto.getSize().equalsIgnoreCase("SMALL")) {
            food.setSize(ESize.SMALL);
        }
        if (foodDto.getSize().equalsIgnoreCase("MEDIUM")) {
            food.setSize(ESize.MEDIUM);
        }
        if (foodDto.getSize().equalsIgnoreCase("LARGE")) {
            food.setSize(ESize.LARGE);
        }
        food.setCode(code);
        food.setImage(uploadLink);
        food.setCreatedDate(LocalDateTime.now());
        food.setCategoryFood(cateogryFoodRepository.findById(foodDto.getCategoryId())
                .orElseThrow(() -> new AppException("Category not found with id: " + foodDto.getId(), HttpStatus.NOT_FOUND)));
        foodRepository.save(food);
        modelMapper.map(food, FoodDto.class);
    }

    @Override
    public FoodDto getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Food not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(food, FoodDto.class);

    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    @Override
    public void updateFood(FoodDto foodDto, MultipartFile multipartFile) throws IOException {
        Food food = foodRepository.findById(foodDto.getId())
                .orElseThrow(() -> new AppException("Food not found with id: " + foodDto.getId(), HttpStatus.NOT_FOUND));
        if (multipartFile != null && !multipartFile.isEmpty()) {
            // Xóa hình ảnh cũ trên AWS
            String imageUrl = food.getImage();
            String imageKey = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            imageKey = imageKey.replace("%3A", ":");
            amazonS3.deleteObject(BUCKET_NAME_FOOD, imageKey);
            // Lưu hình ảnh mới lên AWS
            String newImageName = food.getCode() + "_" + LocalDateTime.now() + getFileExtension(multipartFile.getOriginalFilename());
            File newImageFile = convertMultiPartFileToFile(multipartFile);
            amazonS3.putObject(new PutObjectRequest(BUCKET_NAME_FOOD, newImageName, newImageFile));
            newImageFile.delete();
            String newImageLink = amazonS3.getUrl(BUCKET_NAME_FOOD, newImageName).toString();
            // Cập nhật link hình ảnh mới trong movieDTO
            food.setImage(newImageLink);
        } else {
            food.setImage(food.getImage());
        }

        if (!foodDto.getName().isEmpty() && !foodDto.getName().isBlank()) {
            if (foodRepository.findByName(foodDto.getName()).isPresent()) {
                throw new AppException("name: " + foodDto.getName() + " already exists", HttpStatus.BAD_REQUEST);
            }
            food.setName(foodDto.getName());
        } else {
            food.setName(food.getName());
        }
        if (foodDto.getPrice() > 0) {
            food.setPrice(foodDto.getPrice());
        } else {
            food.setPrice(food.getPrice());
        }
        if (!foodDto.getSize().isEmpty() && !foodDto.getSize().isBlank()) {
            if (foodDto.getSize().equalsIgnoreCase("SMALL")) {
                food.setSize(ESize.SMALL);
            }
            if (foodDto.getSize().equalsIgnoreCase("MEDIUM")) {
                food.setSize(ESize.MEDIUM);
            }
            if (foodDto.getSize().equalsIgnoreCase("LARGE")) {
                food.setSize(ESize.LARGE);
            }
        } else {
            food.setSize(food.getSize());
        }
        if (foodDto.isStatus() != food.isStatus()) {
            food.setStatus(foodDto.isStatus());
        } else {
            food.setStatus(food.isStatus());
        }

        if (foodDto.getCategoryId() > 0) {
            food.setCategoryFood(cateogryFoodRepository.findById(foodDto.getCategoryId())
                    .orElseThrow(() -> new AppException("Category not found with id: " + foodDto.getId(), HttpStatus.NOT_FOUND)));
        } else {
            food.setCategoryFood(food.getCategoryFood());
        }
        foodRepository.save(food);
        modelMapper.map(food, FoodDto.class);

    }

    @Override
    public void deleteFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new AppException("Food not found with id: " + id, HttpStatus.NOT_FOUND));
        String imageUrl = food.getImage();
        String imageKey = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        imageKey = imageKey.replace("%3A", ":");
        amazonS3.deleteObject(BUCKET_NAME_FOOD, imageKey);
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
        return food.map(f -> modelMapper.map(f, FoodDto.class)).getContent();

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
