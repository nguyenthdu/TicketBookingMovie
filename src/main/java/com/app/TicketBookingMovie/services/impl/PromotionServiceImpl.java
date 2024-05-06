package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PromotionDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Promotion;
import com.app.TicketBookingMovie.models.PromotionLine;
import com.app.TicketBookingMovie.repository.PromotionRepository;
import com.app.TicketBookingMovie.services.PromotionService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromotionServiceImpl implements PromotionService {
    private final PromotionRepository promotionRepository;
    private final ModelMapper modelMapper;

    public PromotionServiceImpl(PromotionRepository promotionRepository, ModelMapper modelMapper) {
        this.promotionRepository = promotionRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createPromotion(PromotionDto promotionDto) {
        Promotion promotion = modelMapper.map(promotionDto, Promotion.class);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = promotionDto.getStartDate();
        LocalDateTime endDate = promotionDto.getEndDate();
        //ngày bắt đầu phải là ngày tiếp theo
        if (startDate.isBefore(LocalDateTime.now()) || startDate.getDayOfMonth() == now.getDayOfMonth()) {
            throw new AppException("Thời gian bắt đầu phải là ngày tiếp theo", HttpStatus.BAD_REQUEST);
        }
        // Check if the end date is after the start date
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new AppException("Thời gian kết thúc phải sau thời gian bắt đầu", HttpStatus.BAD_REQUEST);
        }
        // Check if the promotion period overlaps with any existing promotions
        List<Promotion> existingPromotions = promotionRepository.findAll();
        for (Promotion existingPromotion : existingPromotions) {
            if ((startDate.isBefore(existingPromotion.getEndDate()) || startDate.isEqual(existingPromotion.getEndDate())) &&
                    (endDate.isAfter(existingPromotion.getStartDate()) || endDate.isEqual(existingPromotion.getStartDate()))) {
                throw new AppException("Chương trình khuyến mãi khác đã tồn tại trong khoảng thời gian từ " + startDate + " đến " + endDate + ". Vui lòng chọn khoảng thời gian khác", HttpStatus.BAD_REQUEST);
            }
        }
        promotion.setCreatedAt(LocalDateTime.now());
        promotion.setStatus(false);
        promotionRepository.save(promotion);
    }

    //kích hoạt trạng thái của chương trình khuyến mãi khi thời gian băắt đầu đến
    @Scheduled(fixedRate = 60000) // This will run the method every minute
    public void activatePromotion() {
        promotionRepository.updatePromotionStatus();
        promotionRepository.updatePromotionLineStatus();
    }


    @Override
    public Promotion findPromotionById(Long id) {
        return promotionRepository.findById(id).orElseThrow(() -> new AppException("Không tìm thấy chương trình khuyến mãi với id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public PromotionDto getPromotionById(Long id) {
        Promotion promotion = findPromotionById(id);
        return modelMapper.map(promotion, PromotionDto.class);
    }


    @Override
    public void deletePromotion(Long id) {
        //nếu đã bắt đầu thì không thể xóa
        Promotion promotion = findPromotionById(id);
        if (LocalDateTime.now().isAfter(promotion.getStartDate())) {
            throw new AppException("Chương trình đã bắt đầu và không thể xóa", HttpStatus.BAD_REQUEST);
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
        if (promotionDto.getStartDate() != null && !promotionDto.getStartDate().equals(promotion.getStartDate())) {
            if (LocalDateTime.now().isAfter(promotion.getStartDate())) {
                throw new AppException("Chương trình đã bắt đầu và không thể cập nhật", HttpStatus.BAD_REQUEST);
            }
            if (promotionDto.getStartDate().isBefore(LocalDateTime.now()) && promotionDto.getStartDate().getDayOfMonth() == LocalDateTime.now().getDayOfMonth()) {
                throw new AppException("Ngày bắt đầu phải là ngày tiếp theo", HttpStatus.BAD_REQUEST);
            }

            promotion.setStartDate(promotionDto.getStartDate());
        } else {
            promotion.setStartDate(promotion.getStartDate());
        }
        if (promotionDto.getEndDate() != null && !promotionDto.getEndDate().equals(promotion.getEndDate())) {
            if (promotionDto.getEndDate().isBefore(LocalDateTime.now())) {
                throw new AppException("Ngày kết thúc phải sau ngày hiện tại", HttpStatus.BAD_REQUEST);
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
        if (!promotion.isStatus()) {
            for (PromotionLine promotionLine : promotion.getPromotionLines()) {
                promotionLine.setStatus(false);
            }
        }

    }

    @Override
    public List<PromotionDto> getAllPromotion(Integer page, Integer size, LocalDate startDate,LocalDate endDate, boolean status) {
        List<Promotion> promotions = promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (startDate != null && endDate != null) {
            promotions = promotions.stream().filter(promotion -> promotion.getStartDate().isAfter(startDate.atStartOfDay()) && promotion.getEndDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        } else if (startDate != null) {
            promotions = promotions.stream().filter(promotion -> promotion.getStartDate().isAfter(startDate.atStartOfDay())).collect(Collectors.toList());
        } else if (endDate != null) {
            promotions = promotions.stream().filter(promotion -> promotion.getEndDate().isBefore(endDate.atStartOfDay().plusDays(1))).collect(Collectors.toList());
        }
        //sort by  create at
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, promotions.size());
        return promotions.subList(fromIndex, toIndex).stream().map(promotion -> modelMapper.map(promotion, PromotionDto.class)).collect(Collectors.toList());

    }

    @Override
    public long countAllPromotion(LocalDate startDate, LocalDate endDate, boolean status) {
        if (startDate != null && endDate != null) {
            return promotionRepository.countAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(startDate.atStartOfDay(), endDate.atStartOfDay().plusDays(1), status);
        } else if (startDate != null) {
            return promotionRepository.countAllByStartDateGreaterThanEqualAndStatus(startDate.atStartOfDay(), status);
        } else if (endDate != null) {
            return promotionRepository.countAllByEndDateLessThanEqualAndStatus(endDate.atStartOfDay().plusDays(1), status);
        } else {
            return promotionRepository.countAllByStatus(status);
        }
    }


}
