package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.repository.PriceHeaderRepository;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PriceHeaderServiceImpl implements PriceHeaderService {
    private final PriceHeaderRepository priceHeaderRepository;
    private final ModelMapper modelMapper;

    public String randomCode() {
        return "GG" + LocalDateTime.now().getNano();
    }

    public PriceHeaderServiceImpl(PriceHeaderRepository priceHeaderRepository, ModelMapper modelMapper) {
        this.priceHeaderRepository = priceHeaderRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createPriceHeader(PriceHeaderDto priceHeaderDto) {
        // Parse the startDate and endDate from the priceHeaderDto
        LocalDateTime startDate = priceHeaderDto.getStartDate();
        LocalDateTime endDate = priceHeaderDto.getEndDate();

        // Check if the start date is in the past
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new AppException("Start date cannot be in the past", HttpStatus.BAD_REQUEST);
        }

        // Check if the end date is after the start date
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new AppException("End date must be after start date", HttpStatus.BAD_REQUEST);
        }

        // Check if the time period is already occupied
        boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        if (exists) {
            throw new AppException("A sale price already exists within the specified time period. Start date:", HttpStatus.BAD_REQUEST);
        }

        // Map the priceHeaderDto to a PriceHeader entity
        PriceHeader priceHeader = modelMapper.map(priceHeaderDto, PriceHeader.class);

        // Generate a random code for the priceHeader
        priceHeader.setCode(randomCode());
        priceHeader.setCreatedDate(LocalDateTime.now());
        // Save the priceHeader to the database
        priceHeaderRepository.save(priceHeader);
    }

    //TODO: update không cập nhật ngày bắt đầu
    @Override
    public void updatePriceHeader(PriceHeaderDto priceHeaderDto) {
// Parse the start date and end date from the priceHeaderDto
        LocalDateTime endDate = priceHeaderDto.getEndDate();


        // Find the sale price by its ID
        PriceHeader priceHeader = priceHeaderRepository.findById(priceHeaderDto.getId())
                .orElseThrow(() -> new AppException("Sale price not found with id: " + priceHeaderDto.getId(), HttpStatus.NOT_FOUND));

        // Update the fields of the existing sale price with the new values
        if (!priceHeaderDto.getName().isEmpty() && !priceHeaderDto.getName().isBlank()) {
            priceHeader.setName(priceHeaderDto.getName());
        } else {
            priceHeader.setName(priceHeader.getName());
        }
        if (!priceHeaderDto.getDescription().isEmpty() && !priceHeaderDto.getDescription().isBlank()) {
            priceHeader.setDescription(priceHeaderDto.getDescription());
        } else {
            priceHeader.setDescription(priceHeader.getDescription());
        }
        if (priceHeaderDto.getEndDate() != null) {
            // Check if the end date is after the start date
            if (!endDate.isAfter(priceHeader.getStartDate())) {
                throw new AppException("End date must be after start date", HttpStatus.BAD_REQUEST);
            }
            if (endDate.isBefore(LocalDateTime.now())) {
                throw new AppException("End date must be after current date", HttpStatus.BAD_REQUEST);
            }
            // Check if the time period is already occupied
            boolean exists = priceHeaderRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, priceHeaderDto.getStartDate());
            if (exists) {
                throw new AppException("A sale price already exists within the specified time period. Start date:", HttpStatus.BAD_REQUEST);
            }
            priceHeader.setEndDate(priceHeaderDto.getEndDate());
        } else {
            priceHeader.setEndDate(priceHeader.getEndDate());
        }
        if (priceHeaderDto.isStatus() != priceHeader.isStatus()) {
            priceHeader.setStatus(priceHeaderDto.isStatus());
        } else {
            priceHeader.setStatus(priceHeader.isStatus());
        }

        // Save the updated sale price to the database
        priceHeaderRepository.save(priceHeader);
    }

    @Override
    public PriceHeaderDto getPriceHeaderById(Long id) {
        PriceHeader priceHeader = priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale price not found with id: " + id, HttpStatus.NOT_FOUND));
        //lấy danh sách price detail của price header


        return modelMapper.map(priceHeader, PriceHeaderDto.class);
    }

    @Override
    public void deletePriceHeaderById(Long id) {
        PriceHeader priceHeader = priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale price not found with id: " + id, HttpStatus.NOT_FOUND));
        if (priceHeader.isStatus()) {
            throw new AppException("Sale price is active, can't delete", HttpStatus.BAD_REQUEST);
        } else {
            priceHeaderRepository.delete(priceHeader);
        }

    }

    @Override
    public List<PriceHeaderDto> getAllPriceHeader(Integer page, Integer size, String code, String name, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PriceHeader> pageSalePrice;
        if (code != null && !code.isEmpty()) {
            pageSalePrice = priceHeaderRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            pageSalePrice = priceHeaderRepository.findByNameContaining(name, pageable);

        } else if (startDate != null && endDate != null) {
            pageSalePrice = priceHeaderRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate, pageable);
        } else {
            pageSalePrice = priceHeaderRepository.findAll(pageable);
        }
        return pageSalePrice.map(priceHeader -> modelMapper.map(priceHeader, PriceHeaderDto.class)).getContent();
    }

    @Override
    public long countAllPriceHeader(String code, String name, LocalDateTime startDate, LocalDateTime endDate) {
        if (code != null && !code.isEmpty()) {
            return priceHeaderRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return priceHeaderRepository.countByNameContaining(name);

        } else if (startDate != null && endDate != null) {
            return priceHeaderRepository.countByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate);
        } else {
            return priceHeaderRepository.count();
        }

    }

}
/*
 * Tôi có phương thức thêm chương trình giảm giá sau đây và tôi muốn thêm điều kiện, Tôi muốn tạo giảm giá với điều kiện là nếu khoảng thời gian từ startDate đến endDate không tồn tại (không thể trùng lặp), thời gian bắt đầu không được nhỏ hơn thời gian hiện tại(trong quá khứ),thời gian kết thúc phải sau thời gian bắt đầu:*/