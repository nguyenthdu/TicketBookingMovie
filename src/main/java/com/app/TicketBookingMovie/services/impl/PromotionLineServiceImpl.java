package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDiscountDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class PromotionLineServiceImpl implements PromotionLineService {
    private final PromotionLineRepository promotionLineRepository;
    private final ModelMapper modelMapper;
    private final PromotionDiscountDetailService promotionDiscountDetailService;
    private final PromotionTicketDetailService promotionTicketDetailService;
    private final PromotionFoodDetailService promotionFoodDetailService;
    private final PromotionService promotionService;

    public PromotionLineServiceImpl(PromotionLineRepository promotionLineRepository, ModelMapper modelMapper, PromotionDiscountDetailService promotionDiscountDetailService, PromotionTicketDetailService promotionTicketDetailService, PromotionFoodDetailService promotionFoodDetailService, PromotionService promotionService) {
        this.promotionLineRepository = promotionLineRepository;
        this.modelMapper = modelMapper;
        this.promotionDiscountDetailService = promotionDiscountDetailService;
        this.promotionTicketDetailService = promotionTicketDetailService;
        this.promotionFoodDetailService = promotionFoodDetailService;
        this.promotionService = promotionService;
    }

    public String randomCode() {
        return "KM" + LocalDateTime.now().getNano();
    }

    @Override
    @Transactional
    public void createPromotionLine(PromotionLineDto promotionLineDto) {
        Promotion promotion = promotionService.findPromotionById(promotionLineDto.getPromotionId());
        if (promotionLineDto.getStartDate().isBefore(promotion.getStartDate()) || promotionLineDto.getEndDate().isAfter(promotion.getEndDate())) {
            throw new AppException("Thời gian hoạt động khuyến mãi phải nằm trong thời gian khuyến mãi của: " + promotion.getName() + " là từ ngày: " + promotion.getStartDate() + " đến " + promotion.getEndDate(), HttpStatus.BAD_REQUEST);
        }
        if (promotionLineDto.getStartDate().isAfter(promotionLineDto.getEndDate())) {
            throw new AppException("Ngày bắt đầu không thể sau ngày kết thúc", HttpStatus.BAD_REQUEST);
        }

        PromotionLine promotionLine = modelMapper.map(promotionLineDto, PromotionLine.class);
        switch (promotionLineDto.getTypePromotion()) {
            case "DISCOUNT":
                //kiểm tra xem ngày bắt đầu và ngày kết thúc có trùng với promotion line có cùng loại và cùng 1 promotion
                // Kiểm tra xem ngày bắt đầu và ngày kết thúc có trùng với promotion line có cùng loại và cùng 1 promotion
                if (promotionLineRepository.existsByStartDateAndEndDateAndTypePromotionAndPromotion(
                        promotionLineDto.getStartDate(),
                        promotionLineDto.getEndDate(),
                        ETypePromotion.DISCOUNT, // Chuyển đổi sang ETypePromotion
                        promotion)) {
                    throw new AppException("Khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
                }

                promotionLine.setPromotionDiscountDetail(promotionDiscountDetailService.createPromotionDiscountDetail(promotionLineDto.getPromotionDiscountDetailDto()));
                promotionLine.setTypePromotion(ETypePromotion.valueOf("DISCOUNT"));
                break;
            case "FOOD":
                if (promotionLineRepository.existsByStartDateAndEndDateAndTypePromotionAndPromotion(
                        promotionLineDto.getStartDate(),
                        promotionLineDto.getEndDate(),
                        ETypePromotion.FOOD, // Chuyển đổi sang ETypePromotion
                        promotion)) {
                    throw new AppException("Khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
                }
                promotionLine.setPromotionFoodDetail(promotionFoodDetailService.createPromotionFoodDetail(promotionLineDto.getPromotionFoodDetailDto()));
                promotionLine.setTypePromotion(ETypePromotion.valueOf("FOOD"));
                break;
            case "TICKET":
                if (promotionLineRepository.existsByStartDateAndEndDateAndTypePromotionAndPromotion(
                        promotionLineDto.getStartDate(),
                        promotionLineDto.getEndDate(),
                        ETypePromotion.TICKET, // Chuyển đổi sang ETypePromotion
                        promotion)) {
                    throw new AppException("Khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
                }
                promotionLine.setPromotionTicketDetail(promotionTicketDetailService.createPromotionTicketDetail(promotionLineDto.getPromotionTicketDetailDto()));
                promotionLine.setTypePromotion(ETypePromotion.valueOf("TICKET"));
                break;
        }
        promotionLine.setCode(randomCode());
        promotionLine.setStatus(false);
        promotionLine.setCreatedAt(LocalDateTime.now());
        promotionLineRepository.save(promotionLine);

    }

    @Override
    public List<PromotionLine> getPromotionLineActive() {
        //lấy danh sách promtionLine có thời gian hiện tại nằm trong thời gian khuyến mãi, và status = true
        return promotionLineRepository.findActivePromotionLines(LocalDateTime.now());

    }

    @Override
    public PromotionLineDto showPromotionLineDiscountMatchInvoice(BigDecimal totalPrice) {
        //lấy danh sách promotionLine có thời gian hiện tại nằm trong thời gian khuyến mãi, và status = true
        List<PromotionLine> promotionLines = promotionLineRepository.findActivePromotionLines(LocalDateTime.now());
        PromotionLine promotionLine = promotionLines.stream()
                .filter(line -> line.getTypePromotion().equals(ETypePromotion.DISCOUNT))
                .filter(line -> line.getPromotionDiscountDetail().getMinBillValue().compareTo(totalPrice) <= 0)
                .max(Comparator.comparing(line -> line.getPromotionDiscountDetail().getDiscountValue()))
                .orElse(null);

        if (promotionLine == null) {
            return null;
        }
        PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
        PromotionDiscountDetailDto promotionDiscountDetailDto = modelMapper.map(promotionLine.getPromotionDiscountDetail(), PromotionDiscountDetailDto.class);
        promotionLineDto.setPromotionDiscountDetailDto(promotionDiscountDetailDto);
        return promotionLineDto;
    }

    @Override
    public PromotionLineDto getPromotionLineById(Long promotionLineId) {
        return null;
    }


    @Override
    public List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
        return List.of();
    }

    @Override
    public long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
        return 0;
    }

    @Override
    public void deletePromotionLine(Long promotionLineId) {

    }
//    private final PromotionLineRepository promotionLineRepository;
//    private final PromotionDetailService promotionDetailService;
//    private final PromotionService promotionService;
//    private final ModelMapper modelMapper;
//
//    public PromotionLineServiceImpl(PromotionLineRepository promotionLineRepository, PromotionDetailService promotionDetailService, PromotionService promotionService, ModelMapper modelMapper) {
//        this.promotionLineRepository = promotionLineRepository;
//        this.promotionDetailService = promotionDetailService;
//        this.promotionService = promotionService;
//        this.modelMapper = modelMapper;
//
//    }
//
//
//    @Override
//    @Transactional
//    public void createPromotionLine(PromotionLineDto promotionLineDto) {
////        Promotion promotion = promotionService.findPromotionById(promotionLineDto.getPromotionId());
////        // Kiểm tra code đã tồn tại chưa
////        if (promotion.getPromotionLines().stream().anyMatch(line -> line.getCode().equals(promotionLineDto.getCode()))) {
////            throw new AppException("Mã khuyến mãi đã tồn tại", HttpStatus.BAD_REQUEST);
////        }
////        PromotionLine promotionLine = modelMapper.map(promotionLineDto, PromotionLine.class);
////
////        if (!promotionLine.getStartDate().isAfter(promotion.getStartDate()) || !promotionLine.getEndDate().isBefore(promotion.getEndDate())) {
////            throw new AppException("Thời gian hoạt động khuyến mãi phải nằm trong thời gian khuyến mãi của: " + promotion.getName() + " là từ ngày: " + promotion.getStartDate() + " đến " + promotion.getEndDate(), HttpStatus.BAD_REQUEST);
////        }
////
////        // Check start date and end date
////        if (promotionLine.getStartDate().isAfter(promotionLine.getEndDate())) {
////            throw new AppException("Ngày bắt đầu không thể sau ngày kết thúc", HttpStatus.BAD_REQUEST);
////        }
////
////        // Check đối tượng áp dung
////        switch (promotionLineDto.getApplicableObject()) {
////            case "ALL" -> promotionLine.setApplicableObject(EApplicableObject.ALL);
////            case "LEVEL_NORMAL" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_NORMAL);
////            case "LEVEL_SILVER" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_SILVER);
////            case "LEVEL_GOLD" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_GOLD);
////            case "LEVEL_PLATINUM" -> promotionLine.setApplicableObject(EApplicableObject.LEVEL_PLATINUM);
////            default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
////        }
////
////        if (promotionLineDto.getTypePromotion().equals("GIFT")) {
////            promotionLine.setTypePromotion(ETypePromotion.GIFT);
////            PromotionDiscountDetail promotionDiscountDetail = promotionDetailService.createPromotionDetailGift(promotionLineDto.getPromotionDiscountDetailDto());
////            promotionLine.setPromotionDiscountDetail(promotionDiscountDetail);
////            // Trừ đi số lượng sản phẩm
////            promotionDiscountDetail.getFood().setQuantity(promotionDiscountDetail.getFood().getQuantity() - promotionDiscountDetail.getMaxValue());
////        } else if (promotionLineDto.getTypePromotion().equals("DISCOUNT")) {
////            promotionLine.setTypePromotion(ETypePromotion.DISCOUNT);
////            PromotionDiscountDetail promotionDiscountDetail = promotionDetailService.createPromotionDetailDiscount(promotionLineDto.getPromotionDiscountDetailDto());
////            promotionLine.setPromotionDiscountDetail(promotionDiscountDetail);
////        } else {
////            throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
////        }
////
////        // Nếu status của promotion là false thì không được tạo promotion line
////        if (!promotion.isStatus() && promotionLine.isStatus()) {
////            throw new AppException("Không thể kích hoạt hoạt động khuyến mãi khi chương trình khuyến mãi không hoạt động", HttpStatus.BAD_REQUEST);
////        }
////
////        if (promotion.getEndDate().isBefore(LocalDateTime.now())) {
////            throw new AppException("Không thể tạo khuyến mãi khi chương trình khuyến mãi đã kết thúc", HttpStatus.BAD_REQUEST);
////        }
////
////
////        promotionLineRepository.save(promotionLine);
////        promotion.getPromotionLines().add(promotionLine);
//    }
//
//
//    @Override
//    public PromotionLineDto getPromotionLineById(Long promotionLineId) {
//        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi với id: " + promotionLineId, HttpStatus.NOT_FOUND));
//        //lấy promotiondetail
//        PromotionDiscountDetailDto promotionDetailDto = promotionDetailService.getPromotionDetailByPromotionLineId(promotionLineId);
//        PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
//        promotionLineDto.setPromotionDiscountDetailDto(promotionDetailDto);
//        return promotionLineDto;
//    }
//
//    @Override
//    public List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<PromotionLine> promotionLines;
//
//
//        if (promotionId != null) {
//            promotionLines = promotionLineRepository.findAllByPromotionId(promotionId, pageable);
//        } else if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
//            promotionLines = promotionLineRepository.findAllByCode(promotionLineCode, pageable);
//        } else if (startDate != null && endDate != null) {
//            promotionLines = promotionLineRepository.findAllByStartDateAndEndDate(startDate, endDate, pageable);
//        } else if (applicableObject != null && !applicableObject.isEmpty()) {
//            switch (applicableObject) {
//                case "ALL" ->
//                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.ALL, pageable);
//                case "LEVEL_NORMAL" ->
//                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_NORMAL, pageable);
//                case "LEVEL_SILVER" ->
//                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_SILVER, pageable);
//                case "LEVEL_GOLD" ->
//                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_GOLD, pageable);
//                case "LEVEL_PLATINUM" ->
//                        promotionLines = promotionLineRepository.findAllByApplicableObject(EApplicableObject.LEVEL_PLATINUM, pageable);
//                default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
//            }
//        } else if (typePromotion != null && !typePromotion.isEmpty()) {
//            switch (typePromotion) {
////                case "GIFT" ->
////                        promotionLines = promotionLineRepository.findAllByTypePromotion(ETypePromotion.GIFT, pageable);
//                case "DISCOUNT" ->
//                        promotionLines = promotionLineRepository.findAllByTypePromotion(ETypePromotion.DISCOUNT, pageable);
//                default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
//            }
//        } else {
//            promotionLines = promotionLineRepository.findAll(pageable);
//        }
//        return promotionLines.map(promotionLine -> {
//            PromotionDiscountDetailDto promotionDetailDto = promotionDetailService.getPromotionDetailByPromotionLineId(promotionLine.getId());
//            PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
//            promotionLineDto.setPromotionDiscountDetailDto(promotionDetailDto);
//            return promotionLineDto;
//        }).getContent();
//    }
//
//    @Override
//    public long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String applicableObject, String typePromotion) {
////        //đếm số lương phẩn tử trả về
////        if (promotionId != null) {
////            return promotionLineRepository.countByPromotionId(promotionId);
////        } else if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
////            return promotionLineRepository.countByCode(promotionLineCode);
////        } else if (startDate != null && endDate != null) {
////            return promotionLineRepository.countAllByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate);
////        } else if (applicableObject != null && !applicableObject.isEmpty()) {
////            switch (applicableObject) {
////                case "ALL" -> {
////                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.ALL);
////                }
////                case "LEVEL_NORMAL" -> {
////                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_NORMAL);
////                }
////                case "LEVEL_SILVER" -> {
////                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_SILVER);
////                }
////                case "LEVEL_GOLD" -> {
////                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_GOLD);
////                }
////                case "LEVEL_PLATINUM" -> {
////                    return promotionLineRepository.countAllByApplicableObject(EApplicableObject.LEVEL_PLATINUM);
////                }
////                default -> throw new AppException("Đối tượng áp dụng không hợp lệ", HttpStatus.BAD_REQUEST);
////            }
////        } else if (typePromotion != null && !typePromotion.isEmpty()) {
////            switch (typePromotion) {
////                case "GIFT" -> {
//////                    return promotionLineRepository.countAllByTypePromotion(ETypePromotion.GIFT);
////                }
////                case "DISCOUNT" -> {
////                    return promotionLineRepository.countAllByTypePromotion(ETypePromotion.DISCOUNT);
////                }
////                default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
////            }
////
////        } else {
////            return promotionLineRepository.count();
////        }
//        return 0;
//
//    }
//
//    @Override
//    public void deletePromotionLine(Long promotionLineId) {
//        //nếu như promotion line đã bắt đầu thì không thể xóa
//        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Promotion line not found", HttpStatus.NOT_FOUND));
//        if (LocalDateTime.now().isAfter(promotionLine.getStartDate())) {
//            throw new AppException("Không thể xóa khuyến mãi khi đã bắt đầu", HttpStatus.BAD_REQUEST);
//        }
//        if(LocalDateTime.now().isAfter(promotionLine.getEndDate())){
//            throw new AppException("Không thể xóa khuyến mãi khi đã kết thúc", HttpStatus.BAD_REQUEST);
//        }
//        promotionLineRepository.deleteById(promotionLineId);
//
//    }

}
