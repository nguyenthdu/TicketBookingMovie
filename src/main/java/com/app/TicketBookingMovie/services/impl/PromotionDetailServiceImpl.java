package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.PromotionDetail;
import com.app.TicketBookingMovie.repository.PromotionDetailRepository;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.services.FoodService;
import com.app.TicketBookingMovie.services.PromotionDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PromotionDetailServiceImpl implements PromotionDetailService {
    private final PromotionDetailRepository promotionDetailRepository;
    private final PromotionLineRepository promotionLineRepository;
    private final ModelMapper modelMapper;
    private final FoodService foodService;


    public PromotionDetailServiceImpl(PromotionDetailRepository promotionDetailRepository, PromotionLineRepository promotionLineRepository, ModelMapper modelMapper, FoodService foodService) {
        this.promotionDetailRepository = promotionDetailRepository;
        this.promotionLineRepository = promotionLineRepository;
        this.modelMapper = modelMapper;
        this.foodService = foodService;
    }

    @Override
    public PromotionDetail createPromotionDetailGift(PromotionDetailDto promotionDetailDto) {
        // Kiểm tra xem sản phẩm đã được thêm vào trong cùng một promotion detail của promotion line nào khác chưa
        boolean isProductAdded = promotionLineRepository.existsByPromotionDetail_Food_Id(promotionDetailDto.getFoodId());

        if (isProductAdded) {
            // Nếu sản phẩm đã tồn tại, bạn có thể trả về null hoặc ném ra một ngoại lệ
            // Ở đây, tôi sẽ ném ra một ngoại lệ để báo hiệu rằng sản phẩm đã tồn tại và không thể thêm vào nữa
            throw new AppException("Đồ ăn này đã tồn tại trong chương trình khuyến mãi này, vui lòng chọn đồ ăn khác", HttpStatus.BAD_REQUEST);
        }

        // Nếu sản phẩm chưa tồn tại, tiếp tục tạo PromotionDetail
        PromotionDetail promotionDetail = modelMapper.map(promotionDetailDto, PromotionDetail.class);
        Food food = foodService.findById(promotionDetailDto.getFoodId());
        promotionDetail.setFood(food);
        promotionDetail.setDiscountValue(0);

        //neu trang thai cua header la false thi se set gia tri cua promotion detail la 0
        return promotionDetailRepository.save(promotionDetail);
    }

    @Override
    public PromotionDetail createPromotionDetailDiscount(PromotionDetailDto promotionDetailDto) {
        PromotionDetail promotionDetail = modelMapper.map(promotionDetailDto, PromotionDetail.class);
        promotionDetail.setFood(null);
        return promotionDetailRepository.save(promotionDetail);
    }


    @Override
    public PromotionDetailDto getPromotionDetailByPromotionLineId(Long promotionLineId) {
        PromotionDetail promotionDetail = promotionDetailRepository.findByPromotionLineId(promotionLineId).orElseThrow(() -> new AppException("Promotion detail not found", HttpStatus.NOT_FOUND));
        return modelMapper.map(promotionDetail, PromotionDetailDto.class);

    }


}