package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDiscountDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionFoodDetailDto;
import com.app.TicketBookingMovie.dtos.PromotionLineDto;
import com.app.TicketBookingMovie.dtos.PromotionTicketDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.ETypePromotion;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final AwsService awsService;

    public PromotionLineServiceImpl(PromotionLineRepository promotionLineRepository, ModelMapper modelMapper, PromotionDiscountDetailService promotionDiscountDetailService, PromotionTicketDetailService promotionTicketDetailService, PromotionFoodDetailService promotionFoodDetailService, PromotionService promotionService, AwsService awsService) {
        this.promotionLineRepository = promotionLineRepository;
        this.modelMapper = modelMapper;
        this.promotionDiscountDetailService = promotionDiscountDetailService;
        this.promotionTicketDetailService = promotionTicketDetailService;
        this.promotionFoodDetailService = promotionFoodDetailService;
        this.promotionService = promotionService;
        this.awsService = awsService;
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
    public void updatePromotionLine(PromotionLineDto promotionLineDto) {
        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineDto.getId()).orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi với id: " + promotionLineDto.getId(), HttpStatus.NOT_FOUND));
        Promotion promotion = promotionLine.getPromotion();
        if (promotionLineDto.getStartDate() != null || promotionLineDto.getEndDate() != null) {
            if (promotionLineDto.getStartDate().isBefore(promotion.getStartDate()) || promotionLineDto.getEndDate().isAfter(promotion.getEndDate())) {
                throw new AppException("Thời gian hoạt động khuyến mãi phải nằm trong thời gian khuyến mãi của: " + promotion.getName() + " là từ ngày: " + promotion.getStartDate() + " đến " + promotion.getEndDate(), HttpStatus.BAD_REQUEST);
            }
            if (promotionLineDto.getStartDate().isAfter(promotionLineDto.getEndDate())) {
                throw new AppException("Ngày bắt đầu không thể sau ngày kết thúc", HttpStatus.BAD_REQUEST);
            }
            //nếu chương trình khuyến mãi đã bắt đầu thì không được cập nhật ngày bắt đầu
            if (!promotionLineDto.getStartDate().equals(promotionLine.getStartDate())) {
                if (LocalDateTime.now().isAfter(promotionLine.getStartDate())) {
                    throw new AppException("Không thể cập nhật ngày bắt đầu khi chương trình khuyến mãi đã bắt đầu", HttpStatus.BAD_REQUEST);
                }
                promotionLine.setStartDate(promotionLineDto.getStartDate());

            } else {
                promotionLine.setStartDate(promotionLine.getStartDate());
            }
            if (!promotionLineDto.getEndDate().equals(promotionLine.getEndDate())) {
                if (LocalDateTime.now().isAfter(promotionLine.getEndDate())) {
                    throw new AppException("Không thể cập nhật ngày kết thúc khi chương trình khuyến mãi đã kết thúc", HttpStatus.BAD_REQUEST);
                }
                promotionLine.setEndDate(promotionLineDto.getEndDate());
            } else {
                promotionLine.setEndDate(promotionLine.getEndDate());
            }
        }


        if (!promotionLineDto.getName().isEmpty() && !promotionLineDto.getName().isBlank() && !promotionLineDto.getName().equals(promotionLine.getName())) {
            promotionLine.setName(promotionLineDto.getName());
        } else {
            promotionLine.setName(promotionLine.getName());
        }
        if (!promotionLineDto.getDescription().isEmpty() && !promotionLineDto.getDescription().isBlank() && !promotionLineDto.getDescription().equals(promotionLine.getDescription())) {
            promotionLine.setDescription(promotionLineDto.getDescription());
        } else {
            promotionLine.setDescription(promotionLine.getDescription());
        }
        if (!promotionLineDto.getImage().isBlank() && !promotionLineDto.getImage().isEmpty() && !promotionLineDto.getImage().equals(promotionLine.getImage())) {
            awsService.deleteImage(promotionLine.getImage());
            promotionLine.setImage(promotionLineDto.getImage());
        } else {
            promotionLine.setImage(promotionLine.getImage());
        }
        if (promotionLineDto.isStatus() != promotionLine.isStatus()) {
            promotionLine.setStatus(promotionLineDto.isStatus());

        } else {
            promotionLine.setStatus(promotionLine.isStatus());
        }

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
    public PromotionLineDto showPromotionLineFoodMatchInvoice(Long foodId, int quantity) {
        //Lấy danh sách promotionLine có thời gian hiện tại nằm trong thời gian khuyến mãi và status = true
        List<PromotionLine> promotionLines = promotionLineRepository.findActivePromotionLines(LocalDateTime.now());
        PromotionLine promotionLine = promotionLines.stream()
                .filter(line -> line.getTypePromotion().equals(ETypePromotion.FOOD))
                .filter(line -> line.getPromotionFoodDetail().getFoodRequired().equals(foodId))
                .filter(line -> line.getPromotionFoodDetail().getQuantityRequired() <= quantity)
                .max(Comparator.comparing(PromotionLine::getCreatedAt))
                .orElse(null);
        if (promotionLine == null) {
            return null;
        } else {
            PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
            PromotionFoodDetailDto promotionFoodDetailDto = modelMapper.map(promotionLine.getPromotionFoodDetail(), PromotionFoodDetailDto.class);
            promotionLineDto.setPromotionFoodDetailDto(promotionFoodDetailDto);
            return promotionLineDto;
        }
    }

    @Override
    public PromotionLineDto showPromotionLineTicketMatchInvoice(Long typeSeatId, int quantity) {
        //Lấy danh sách promotionLine có thời gian hiện tại nằm trong thời gian khuyến mãi và status = true
        List<PromotionLine> promotionLines = promotionLineRepository.findActivePromotionLines(LocalDateTime.now());
        PromotionLine promotionLine = promotionLines.stream()
                .filter(line -> line.getTypePromotion().equals(ETypePromotion.TICKET))
                .filter(line -> line.getPromotionTicketDetail().getTypeSeatRequired().equals(typeSeatId))
                .filter(line -> line.getPromotionTicketDetail().getQuantityRequired() <= quantity)
                .max(Comparator.comparing(PromotionLine::getCreatedAt))
                .orElse(null);
        if (promotionLine == null) {
            return null;
        } else {
            PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
            PromotionTicketDetailDto promotionTicketDetailDto = modelMapper.map(promotionLine.getPromotionTicketDetail(), PromotionTicketDetailDto.class);
            promotionLineDto.setPromotionTicketDetailDto(promotionTicketDetailDto);
            return promotionLineDto;
        }
    }


    @Override
    public PromotionLineDto getPromotionLineById(Long promotionLineId) {
        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi với id: " + promotionLineId, HttpStatus.NOT_FOUND));
        //lấy promotiondetail
        PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
        if (promotionLine.getTypePromotion().equals(ETypePromotion.DISCOUNT)) {
            PromotionDiscountDetailDto promotionDiscountDetailDto = modelMapper.map(promotionLine.getPromotionDiscountDetail(), PromotionDiscountDetailDto.class);
            promotionLineDto.setPromotionDiscountDetailDto(promotionDiscountDetailDto);
        }
        if (promotionLine.getTypePromotion().equals(ETypePromotion.FOOD)) {
            PromotionFoodDetailDto promotionFoodDetailDto = modelMapper.map(promotionLine.getPromotionFoodDetail(), PromotionFoodDetailDto.class);
            promotionLineDto.setPromotionFoodDetailDto(promotionFoodDetailDto);
        }
        if (promotionLine.getTypePromotion().equals(ETypePromotion.TICKET)) {
            //lấy promotion ticket detail
            PromotionTicketDetailDto promotionTicketDetailDto = modelMapper.map(promotionLine.getPromotionTicketDetail(), PromotionTicketDetailDto.class);
            promotionLineDto.setPromotionTicketDetailDto(promotionTicketDetailDto);
        }


        return promotionLineDto;
    }

    @Override
    public List<PromotionLineDto> getAllPromotionLineFromPromotionId(Integer page, Integer size, Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String typePromotion) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PromotionLine> promotionLines;


        if (promotionId != null) {
            if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
                promotionLines = promotionLineRepository.findAllByPromotionIdAndCode(promotionId, promotionLineCode, pageable);
            } else if (startDate != null && endDate != null) {
                promotionLines = promotionLineRepository.findAllByPromotionIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(promotionId, startDate, endDate, pageable);
            } else if (typePromotion != null && !typePromotion.isEmpty()) {
                switch (typePromotion) {
                    case "DISCOUNT" -> {
                        promotionLines = promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.DISCOUNT, pageable);
                    }
                    case "FOOD" -> {
                        promotionLines = promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.FOOD, pageable);
                    }
                    case "TICKET" -> {
                        promotionLines = promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.TICKET, pageable);
                    }
                    default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
                }
            } else {
                promotionLines = promotionLineRepository.findAllByPromotionId(promotionId, pageable);
            }
        } else {

            promotionLines = promotionLineRepository.findAll(pageable);
        }
        return promotionLines.stream().sorted(Comparator.comparing(PromotionLine::getCreatedAt).reversed()).map(promotionLine -> {
            PromotionLineDto promotionLineDto = modelMapper.map(promotionLine, PromotionLineDto.class);
            if (promotionLine.getTypePromotion().equals(ETypePromotion.DISCOUNT)) {
                PromotionDiscountDetailDto promotionDiscountDetailDto = modelMapper.map(promotionLine.getPromotionDiscountDetail(), PromotionDiscountDetailDto.class);
                promotionLineDto.setPromotionDiscountDetailDto(promotionDiscountDetailDto);
            }
            if (promotionLine.getTypePromotion().equals(ETypePromotion.FOOD)) {
                PromotionFoodDetailDto promotionFoodDetailDto = modelMapper.map(promotionLine.getPromotionFoodDetail(), PromotionFoodDetailDto.class);
                promotionLineDto.setPromotionFoodDetailDto(promotionFoodDetailDto);
            }
            if (promotionLine.getTypePromotion().equals(ETypePromotion.TICKET)) {
                PromotionTicketDetailDto promotionTicketDetailDto = modelMapper.map(promotionLine.getPromotionTicketDetail(), PromotionTicketDetailDto.class);
                promotionLineDto.setPromotionTicketDetailDto(promotionTicketDetailDto);
            }
            return promotionLineDto;
        }).toList();

    }

    @Override
    public long countAllPromotionLineFromPromotionId(Long promotionId, String promotionLineCode, LocalDateTime startDate, LocalDateTime endDate, String typePromotion) {
        if (promotionId != null) {
            if (promotionLineCode != null && !promotionLineCode.isEmpty()) {
                return promotionLineRepository.findAllByPromotionIdAndCode(promotionId, promotionLineCode, Pageable.unpaged()).getTotalElements();
            } else if (startDate != null && endDate != null) {
                return promotionLineRepository.findAllByPromotionIdAndStartDateGreaterThanEqualAndEndDateLessThanEqual(promotionId, startDate, endDate, Pageable.unpaged()).getTotalElements();
            } else if (typePromotion != null && !typePromotion.isEmpty()) {
                switch (typePromotion) {
                    case "DISCOUNT" -> {
                        return promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.DISCOUNT, Pageable.unpaged()).getTotalElements();
                    }
                    case "FOOD" -> {
                        return promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.FOOD, Pageable.unpaged()).getTotalElements();
                    }
                    case "TICKET" -> {
                        return promotionLineRepository.findAllByPromotionIdAndTypePromotion(promotionId, ETypePromotion.TICKET, Pageable.unpaged()).getTotalElements();
                    }
                    default -> throw new AppException("Loại khuyến mãi không hợp lệ", HttpStatus.BAD_REQUEST);
                }
            } else {
                return promotionLineRepository.findAllByPromotionId(promotionId, Pageable.unpaged()).getTotalElements();
            }
        } else {
            return promotionLineRepository.findAll(Pageable.unpaged()).getTotalElements();
        }


    }

    @Override
    public void deletePromotionLine(Long promotionLineId) {
        //nếu như promotion line đã bắt đầu thì không thể xóa
        PromotionLine promotionLine = promotionLineRepository.findById(promotionLineId).orElseThrow(() -> new AppException("Promotion line not found", HttpStatus.NOT_FOUND));
        if (LocalDateTime.now().isAfter(promotionLine.getStartDate())) {
            throw new AppException("Không thể xóa khuyến mãi khi đã bắt đầu", HttpStatus.BAD_REQUEST);
        }
        if (LocalDateTime.now().isAfter(promotionLine.getEndDate())) {
            throw new AppException("Không thể xóa khuyến mãi khi đã kết thúc", HttpStatus.BAD_REQUEST);
        }
        promotionLineRepository.deleteById(promotionLineId);

    }

}
