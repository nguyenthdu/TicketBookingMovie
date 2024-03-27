package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.SalePriceDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.SalePrice;
import com.app.TicketBookingMovie.repository.SalePriceRepository;
import com.app.TicketBookingMovie.services.SalePriceService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SalePriceServiceImpl implements SalePriceService {
    private final SalePriceRepository salePriceRepository;
    private final ModelMapper modelMapper;

    public String randomCode() {
        return "GG"+LocalDateTime.now().getNano();
    }

    public SalePriceServiceImpl(SalePriceRepository salePriceRepository, ModelMapper modelMapper) {
        this.salePriceRepository = salePriceRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public void createSalePrice(SalePriceDto salePriceDto) {
        // Parse the startDate and endDate from the salePriceDto
        LocalDateTime startDate = salePriceDto.getStartDate();
        LocalDateTime endDate = salePriceDto.getEndDate();

        // Check if the start date is in the past
        if (startDate.isBefore(LocalDateTime.now())) {
            throw new AppException("Start date cannot be in the past", HttpStatus.BAD_REQUEST);
        }

        // Check if the end date is after the start date
        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new AppException("End date must be after start date", HttpStatus.BAD_REQUEST);
        }

        // Check if the time period is already occupied
        boolean exists = salePriceRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
        if (exists) {
            throw new AppException("A sale price already exists within the specified time period", HttpStatus.BAD_REQUEST);
        }

        // Map the salePriceDto to a SalePrice entity
        SalePrice salePrice = modelMapper.map(salePriceDto, SalePrice.class);

        // Generate a random code for the salePrice
        salePrice.setCode(randomCode());
        salePrice.setCreatedDate(LocalDateTime.now());
        // Save the salePrice to the database
        salePriceRepository.save(salePrice);
    }

    //TODO: update không cập nhật ngày bắt đầu
    @Override
    public void updateSalePrice(SalePriceDto salePriceDto) {
// Parse the start date and end date from the salePriceDto
        LocalDateTime endDate = salePriceDto.getEndDate();

        // Check if the end date is after the start date
        if (endDate.isBefore(salePriceDto.getStartDate()) || endDate.isEqual(salePriceDto.getStartDate())) {
            throw new AppException("End date must be after start date", HttpStatus.BAD_REQUEST);
        }

        // Check if the time period is already occupied
        boolean exists = salePriceRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, salePriceDto.getStartDate());
        if (exists) {
            throw new AppException("A sale price already exists within the specified time period", HttpStatus.BAD_REQUEST);
        }

        // Find the sale price by its ID
        SalePrice salePrice = salePriceRepository.findById(salePriceDto.getId())
                .orElseThrow(() -> new AppException("Sale price not found with id: " + salePriceDto.getId(), HttpStatus.NOT_FOUND));

        // Update the fields of the existing sale price with the new values
        if (!salePriceDto.getName().isEmpty() && !salePriceDto.getName().isBlank()) {
            salePrice.setName(salePriceDto.getName());
        } else {
            salePrice.setName(salePrice.getName());
        }
        if (!salePriceDto.getDescription().isEmpty() && !salePriceDto.getDescription().isBlank()) {
            salePrice.setDescription(salePriceDto.getDescription());
        } else {
            salePrice.setDescription(salePrice.getDescription());
        }
        if (salePriceDto.getEndDate() != null) {
            salePrice.setEndDate(salePriceDto.getEndDate());
        } else {
            salePrice.setEndDate(salePrice.getEndDate());
        }
        if (salePriceDto.isStatus() != salePrice.isStatus()) {
            salePrice.setStatus(salePriceDto.isStatus());
        } else {
            salePrice.setStatus(salePrice.isStatus());
        }

        // Save the updated sale price to the database
        salePriceRepository.save(salePrice);
    }

    @Override
    public SalePriceDto getSalePriceById(Long id) {
        SalePrice salePrice = salePriceRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale price not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(salePrice, SalePriceDto.class);
    }

    @Override
    public void deleteSalePriceById(Long id) {
        SalePrice salePrice = salePriceRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale price not found with id: " + id, HttpStatus.NOT_FOUND));
        if(salePrice.isStatus()){
            throw new AppException("Sale price is active, can't delete", HttpStatus.BAD_REQUEST);
        }
        else {
            salePriceRepository.delete(salePrice);
        }

    }

    @Override
    public List<SalePriceDto> getAllSalePrice(Integer page, Integer size, String code, String name, boolean status, LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SalePrice> pageSalePrice;
        if (code != null && !code.isEmpty()) {
            pageSalePrice = salePriceRepository.findByCodeContaining(code, pageable);
        } else if (name != null && !name.isEmpty()) {
            pageSalePrice = salePriceRepository.findByNameContaining(name, pageable);
        } else if (!status) {
            pageSalePrice = salePriceRepository.findByStatus(false, pageable);
        } else if (startDate != null && endDate != null) {
            pageSalePrice = salePriceRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate, pageable);
        } else {
            pageSalePrice = salePriceRepository.findAll(pageable);
        }
        return pageSalePrice.map(salePrice -> modelMapper.map(salePrice, SalePriceDto.class)).getContent();
    }

    @Override
    public long countAllSalePrice(String code, String name, boolean status, LocalDateTime startDate, LocalDateTime endDate) {
        if (code != null && !code.isEmpty()) {
            return salePriceRepository.countByCodeContaining(code);
        } else if (name != null && !name.isEmpty()) {
            return salePriceRepository.countByNameContaining(name);
        } else if (!String.valueOf(status).isEmpty()) {
            return salePriceRepository.countByStatus(status);
        } else if (startDate != null && endDate != null) {
            return salePriceRepository.countByStartDateGreaterThanEqualAndEndDateLessThanEqual(startDate, endDate);
        } else {
            return salePriceRepository.count();
        }

    }

}
/*
 * Tôi có phương thức thêm chương trình giảm giá sau đây và tôi muốn thêm điều kiện, Tôi muốn tạo giảm giá với điều kiện là nếu khoảng thời gian từ startDate đến endDate không tồn tại (không thể trùng lặp), thời gian bắt đầu không được nhỏ hơn thời gian hiện tại(trong quá khứ),thời gian kết thúc phải sau thời gian bắt đầu:*/