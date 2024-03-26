package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.SalePriceDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.SalePrice;
import com.app.TicketBookingMovie.models.SalePriceDetail;
import com.app.TicketBookingMovie.models.TypeSeat;
import com.app.TicketBookingMovie.models.enums.ETypeDiscount;
import com.app.TicketBookingMovie.repository.FoodRepository;
import com.app.TicketBookingMovie.repository.SalePriceDetailRepository;
import com.app.TicketBookingMovie.repository.SalePriceRepository;
import com.app.TicketBookingMovie.repository.TypeSeatRepository;
import com.app.TicketBookingMovie.services.SalePriceDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class SalePriceServiceDetailImpl implements SalePriceDetailService {
    private final SalePriceDetailRepository salePriceDetailRepository;
    private final ModelMapper modelMapper;
    private final SalePriceRepository salePriceRepository;
    private final FoodRepository foodRepository;
    private final TypeSeatRepository typeSeatRepository;

    public SalePriceServiceDetailImpl(SalePriceDetailRepository salePriceDetailRepository, ModelMapper modelMapper, SalePriceRepository salePriceRepository, FoodRepository foodRepository, TypeSeatRepository typeSeatRepository) {
        this.salePriceDetailRepository = salePriceDetailRepository;
        this.modelMapper = modelMapper;
        this.salePriceRepository = salePriceRepository;
        this.foodRepository = foodRepository;
        this.typeSeatRepository = typeSeatRepository;

    }


    @Override
    public void createSalePriceDetail(Set<SalePriceDetailDto> salePriceDetailDtos) {
        for (SalePriceDetailDto salePriceDetailDto : salePriceDetailDtos) {
            SalePriceDetail salePriceDetail = modelMapper.map(salePriceDetailDto, SalePriceDetail.class);
            SalePrice salePrice = salePriceRepository.findById(salePriceDetailDto.getSalePriceId())
                    .orElseThrow(() -> new AppException("Sale Price not found with id: " + salePriceDetailDto.getSalePriceId(), HttpStatus.NOT_FOUND));
            SalePriceDetail existSalePriceDetail = salePriceDetailRepository.findByTypeSeatIdAndFoodId(salePriceDetailDto.getTypeSeatId(), salePriceDetailDto.getFoodId());
            if (existSalePriceDetail != null && existSalePriceDetail.getSalePrice().getId().equals(salePrice.getId())) {
                throw new AppException("Sale Price Detail already exist", HttpStatus.BAD_REQUEST);
            } else {
                if (salePriceDetailDto.getTypeSeatId() != null && salePriceDetailDto.getTypeSeatId() > 0) {
                    TypeSeat typeSeat = typeSeatRepository.findById(salePriceDetailDto.getTypeSeatId())
                            .orElseThrow(() -> new AppException("Type seat not found", HttpStatus.NOT_FOUND));
                    salePriceDetail.setTypeSeat(typeSeat);
                    if (salePriceDetailDto.getTypeDiscount().equals(ETypeDiscount.PERCENT.name())) {
                        salePriceDetail.setPriceDecrease(typeSeat.getPrice() * salePriceDetail.getDiscount() / 100);
                    } else if (salePriceDetailDto.getTypeDiscount().equals(ETypeDiscount.AMOUNT.name())) {
                        salePriceDetail.setPriceDecrease(typeSeat.getPrice() - salePriceDetail.getDiscount());
                    }
                } else if (salePriceDetailDto.getFoodId() != null && salePriceDetailDto.getFoodId() > 0) {
                    Food food = foodRepository.findById(salePriceDetailDto.getFoodId())
                            .orElseThrow(() -> new AppException("Food not found", HttpStatus.NOT_FOUND));
                    salePriceDetail.setFood(food);
                    if (salePriceDetailDto.getTypeDiscount().equals(ETypeDiscount.PERCENT.name())) {
                        salePriceDetail.setPriceDecrease(food.getPrice() * salePriceDetail.getDiscount() / 100);
                    } else if (salePriceDetailDto.getTypeDiscount().equals(ETypeDiscount.AMOUNT.name())) {
                        salePriceDetail.setPriceDecrease(food.getPrice() - salePriceDetail.getDiscount());
                    }
                }
                salePriceDetail.setCreatedDate(LocalDateTime.now());
            }
            salePrice.getSalePriceDetails().add(salePriceDetail);
            salePriceDetailRepository.save(salePriceDetail);
        }
    }

    @Override
    public SalePriceDetailDto getSalePriceDetail(Long id) {
        SalePriceDetail salePriceDetail = salePriceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale Price Detail not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(salePriceDetail, SalePriceDetailDto.class);
    }

    @Override
    public void updateStatusSalePriceDetail(Long id) {
        SalePriceDetail salePriceDetail = salePriceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale Price Detail not found with id: " + id, HttpStatus.NOT_FOUND));
        //chuyển status thành false
        salePriceDetail.setStatus(false);
        salePriceDetailRepository.save(salePriceDetail);

    }

    @Override
    public Set<SalePriceDetailDto> getAllSalePriceDetail(Long id) {
        SalePrice salePrice = salePriceRepository.findById(id)
                .orElseThrow(() -> new AppException("Sale Price not found with id: " + id, HttpStatus.NOT_FOUND));
        return modelMapper.map(salePrice.getSalePriceDetails(), Set.class);
    }


}
