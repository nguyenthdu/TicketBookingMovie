package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.models.enums.EApplicableObject;
import com.app.TicketBookingMovie.repository.PromotionLineRepository;
import com.app.TicketBookingMovie.repository.PromotionRepository;
import com.app.TicketBookingMovie.services.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final ModelMapper modelMapper;
    private final PromotionLineRepository promotionLineRepository;

    public PromotionServiceImpl(PromotionRepository promotionRepository, ModelMapper modelMapper, PromotionLineRepository promotionLineRepository) {
        this.promotionRepository = promotionRepository;
        this.modelMapper = modelMapper;
        this.promotionLineRepository = promotionLineRepository;
    }

    @Override
    public void createPromotion(PromotionDto promotionDto) {
        Promotion promotion = modelMapper.map(promotionDto, Promotion.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = promotionDto.getStartDate();
        LocalDateTime endDate = promotionDto.getEndDate();
        // Check if the start date is at least one day after the current date
        if (!startDate.isAfter(now.plusDays(1))) {
            throw new AppException("The start date must be at least one day after the current date", HttpStatus.BAD_REQUEST);
        }
        // Check if the end date is after the start date
        if (!endDate.isAfter(startDate)) {
            throw new AppException("The end date must be after the start date", HttpStatus.BAD_REQUEST);
        }
        // Check if the promotion period overlaps with any existing promotions
        List<Promotion> existingPromotions = promotionRepository.findAll();
        for (Promotion existingPromotion : existingPromotions) {
            if ((startDate.isBefore(existingPromotion.getEndDate()) || startDate.isEqual(existingPromotion.getEndDate())) &&
                    (endDate.isAfter(existingPromotion.getStartDate()) || endDate.isEqual(existingPromotion.getStartDate()))) {
                throw new AppException("The promotion period overlaps with an existing promotion", HttpStatus.BAD_REQUEST);
            }
        }
        promotionRepository.save(promotion);
    }

    @Override
    public Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() -> new AppException("Promotion not found", HttpStatus.NOT_FOUND));
    }

    @Override
    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = findPromotionById(id);
        return modelMapper.map(promotion, PromotionDto.class);
    }

    @Override
    public List<PromotionLine> getAllPromotionFitBill(double totalValueBill, LocalDateTime dateTime, String applicableObject) {
        // Lấy danh sách tất cả các promotion
        List<Promotion> promotions = promotionRepository.findAll();

        // Lọc các promotion phù hợp với hóa đơn hiện tại
        return promotions.stream()
                .filter(promotion -> isValidPromotion(promotion, totalValueBill, dateTime, applicableObject))
                .map(promotion -> promotion.getPromotionLines().stream()
                        .filter(promotionLine -> isValidPromotionLine(promotionLine, totalValueBill, applicableObject))
                        .collect(Collectors.toList()))
                .flatMap(List::stream)
                .collect(Collectors.toList());

    }

    private boolean isValidPromotion(Promotion promotion, double totalValueBill, LocalDateTime dateTime, String applicableObject) {
        // Kiểm tra xem ngày hiện tại có nằm trong khoảng thời gian của promotion không
        if (dateTime.isBefore(promotion.getStartDate()) || dateTime.isAfter(promotion.getEndDate())) {
            return false;
        }

        // Kiểm tra trạng thái của promotion
        if (!promotion.isStatus()) {
            return false;
        }

        // Lọc các promotion lines phù hợp
        return promotion.getPromotionLines().stream()
                .filter(PromotionLine::isStatus).anyMatch(promotionLine -> isValidPromotionLine(promotionLine, totalValueBill, applicableObject));
    }

    private boolean isValidPromotionLine(PromotionLine promotionLine, double totalValueBill, String applicableObject) {
        // Kiểm tra xem đối tượng sử dụng (applicableObject) có phù hợp không
        if (!applicableObject.equals(EApplicableObject.ALL.toString()) && !applicableObject.equals(promotionLine.getApplicableObject().toString())) {
            return false;
        }

        // Kiểm tra xem số lượng khuyến mãi còn lại có phù hợp không
        if (promotionLine.getUsePerPromotion() <= 0) {
            return false;
        }

        // Kiểm tra xem tổng giá trị hóa đơn có phù hợp không
        return !(totalValueBill < promotionLine.getPromotionDetail().getMinBillValue());
    }

    @Override
    public PromotionLine getPromotionLineByCodeAndFitBill(String promotionLineCode, double totalValueBill, LocalDateTime dateTime, String applicableObject) {
        // Tìm mã khuyến mãi dựa trên mã được nhập vào
        PromotionLine promotionLine = promotionLineRepository.findByCode(promotionLineCode);

        // Kiểm tra xem mã khuyến mãi có tồn tại không
        if (promotionLine == null) {
            throw new AppException("Promotion line with code " + promotionLineCode + " does not exist", HttpStatus.NOT_FOUND);
        }

        // Kiểm tra xem mã khuyến mãi có phù hợp với hóa đơn không
        if (!isValidPromotionLineCode(promotionLine, totalValueBill, applicableObject, dateTime)) {
            throw new AppException("Promotion line with code " + promotionLineCode + " does not fit the bill", HttpStatus.BAD_REQUEST);
        }

        return promotionLine;
    }

    @Override
    public void deletePromotion(Long id) {
        //nếu đã bắt đầu thì không thể xóa
        Promotion promotion = findPromotionById(id);
        if (LocalDateTime.now().isAfter(promotion.getStartDate())) {
            throw new AppException("Promotion has started and cannot be deleted", HttpStatus.BAD_REQUEST);
        }
        promotionRepository.deleteById(id);
    }

    @Override
    public void updatePromotion(PromotionDto promotionDto) {
        //nếu đã bắt đầu thì không thể update ngày bắt đầu
        Promotion promotion = findPromotionById(promotionDto.getId());
        if (!promotionDto.getName().isEmpty() && !promotionDto.getName().isBlank()) {
            promotion.setName(promotionDto.getName());
        } else {
            promotion.setName(promotion.getName());
        }
        if (LocalDateTime.now().isAfter(promotion.getStartDate())) {
            throw new AppException("Promotion has started and cannot be updated", HttpStatus.BAD_REQUEST);
        } else {
            promotion.setStartDate(promotionDto.getStartDate());
        }
        if (promotionDto.getEndDate() != null && !promotionDto.getEndDate().equals(promotion.getEndDate())) {
            if (promotionDto.getEndDate().isBefore(LocalDateTime.now())) {
                throw new AppException("End date must be after the current date", HttpStatus.BAD_REQUEST);
            }
            promotion.setEndDate(promotionDto.getEndDate());
        } else {
            promotion.setEndDate(promotion.getEndDate());
        }
        if (!promotionDto.getDescription().isEmpty() && !promotionDto.getDescription().isBlank()) {
            promotion.setDescription(promotionDto.getDescription());
        } else {
            promotion.setDescription(promotion.getDescription());
        }
        if (promotionDto.isStatus() != promotion.isStatus()) {
            promotion.setStatus(promotionDto.isStatus());
        } else {
            promotion.setStatus(promotion.isStatus());
        }
        promotionRepository.save(promotion);

    }

    @Override
    public List<PromotionDto> getAllPromotion(Integer page, Integer size, LocalDateTime startDate, LocalDateTime endDate, boolean status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Promotion> promotions;
        if (startDate != null && endDate != null) {
            promotions = promotionRepository.findAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(startDate, endDate, status, pageable);
        } else if (startDate != null) {
            promotions = promotionRepository.findAllByStartDateGreaterThanEqualAndStatus(startDate, status, pageable);
        } else if (endDate != null) {
            promotions = promotionRepository.findAllByEndDateLessThanEqualAndStatus(endDate, status, pageable);
        } else {
            promotions = promotionRepository.findAllByStatus(status, pageable);

        }
        return promotions.stream().map(promotion -> modelMapper.map(promotion, PromotionDto.class)).collect(Collectors.toList());
    }

    @Override
    public long countAllPromotion(LocalDateTime startDate, LocalDateTime endDate, boolean status) {
        if (startDate != null && endDate != null) {
            return promotionRepository.countAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(startDate, endDate, status);
        } else if (startDate != null) {
            return promotionRepository.countAllByStartDateGreaterThanEqualAndStatus(startDate, status);
        } else if (endDate != null) {
            return promotionRepository.countAllByEndDateLessThanEqualAndStatus(endDate, status);
        } else {
            return promotionRepository.countAllByStatus(status);
        }
    }

    private boolean isValidPromotionLineCode(PromotionLine promotionLine, double totalValueBill, String applicableObject, LocalDateTime dateTime) {
        // Kiểm tra xem đối tượng sử dụng (applicableObject) có phù hợp không
        if (!applicableObject.equals(EApplicableObject.ALL.toString()) && !applicableObject.equals(promotionLine.getApplicableObject().toString())) {
            return false;
        }

        // Kiểm tra xem số lượng khuyến mãi còn lại có phù hợp không
        if (promotionLine.getUsePerPromotion() <= 0) {
            return false;
        }

        // Kiểm tra xem tổng giá trị hóa đơn có phù hợp không
        if (totalValueBill < promotionLine.getPromotionDetail().getMinBillValue()) {
            return false;
        }

        // Kiểm tra xem ngày hiện tại có nằm trong khoảng thời gian của mã khuyến mãi không
        if (dateTime.isBefore(promotionLine.getStartDate()) || dateTime.isAfter(promotionLine.getEndDate())) {
            return false;
        }

        // Kiểm tra trạng thái của mã khuyến mãi
        return promotionLine.isStatus();
    }

}
