package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.CategoryFoodDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.CategoryFood;
import com.app.TicketBookingMovie.repository.CateogryFoodRepository;
import com.app.TicketBookingMovie.services.CategoryFoodService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
            throw new AppException("Tên loại đồ ăn: " + categoryFoodDto.getName() + " đã tồn tại!!!", HttpStatus.BAD_REQUEST);
        }
        CategoryFood categoryFood = modelMapper.map(categoryFoodDto, CategoryFood.class);
        categoryFood.setCode(randomCode());
        categoryFood.setCreatedDate(LocalDateTime.now());
        cateogryFoodRepository.save(categoryFood);
        modelMapper.map(categoryFood, CategoryFoodDto.class);

    }

    @Override
    public CategoryFoodDto getCategoryFoodById(Long id) {
       CategoryFood categoryFood = cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy loại đồ ăn với id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(categoryFood, CategoryFoodDto.class);

    }

    @Override
    public CategoryFood findCategoryFoodById(Long id) {
        return cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy loại đồ ăn với id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void updateCategoryFood(CategoryFoodDto categoryFoodDto) {
        CategoryFood categoryFood = cateogryFoodRepository.findById(categoryFoodDto.getId()).orElseThrow(() -> new AppException("Không tìm thấy loại đồ ăn với id: " + categoryFoodDto.getId(), HttpStatus.NOT_FOUND));

        if(!categoryFoodDto.getName().isEmpty() && !categoryFoodDto.getName().isBlank()) {
            if (cateogryFoodRepository.findByName(categoryFoodDto.getName()).isPresent()) {
                throw new AppException("Tên loại đồ ăn: " + categoryFoodDto.getName() + " đã tồn tại!!!", HttpStatus.BAD_REQUEST);
            }
            categoryFood.setName(categoryFoodDto.getName());
        }else{
            categoryFood.setName(categoryFood.getName());
        }
        categoryFood.setCreatedDate(LocalDateTime.now());
        cateogryFoodRepository.save(categoryFood);

    }

    @Override
    public void deleteCategoryFoodById(Long id) {
        CategoryFood categoryFood = cateogryFoodRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy loại đồ ăn với id: " + id, HttpStatus.NOT_FOUND));
        cateogryFoodRepository.delete(categoryFood);
    }

    @Override
    public List<CategoryFoodDto> getAllCategoryFood(int page, int size,String code, String name) {
        List<CategoryFood> pageCategory  = cateogryFoodRepository.findAll(Sort.by(Sort.Direction.DESC, "createdDate"));
        if(code!=null && !code.isEmpty()){
            pageCategory = pageCategory.stream().filter(categoryFood -> categoryFood.getCode().equals(code)).toList();
        }
       else if (name != null && !name.isEmpty()) {
            pageCategory = pageCategory.stream().filter(categoryFood -> categoryFood.getName().toLowerCase().contains(name.toLowerCase())).toList();
        }
        int fromIndex = page  * size;
        int toIndex = Math.min(fromIndex + size, pageCategory.size());
        return pageCategory.subList(fromIndex, toIndex).stream().map(categoryFood -> modelMapper.map(categoryFood, CategoryFoodDto.class)).toList();

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
