package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.models.TypeSeat;
import com.app.TicketBookingMovie.repository.FoodRepository;
import com.app.TicketBookingMovie.repository.PriceDetailRepository;
import com.app.TicketBookingMovie.repository.PriceHeaderRepository;
import com.app.TicketBookingMovie.repository.TypeSeatRepository;
import com.app.TicketBookingMovie.services.PriceDetailService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

@Service
public class PriceDetailServiceImpl implements PriceDetailService {
    private final PriceDetailRepository priceDetailRepository;
    private final ModelMapper modelMapper;
    private final PriceHeaderRepository priceHeaderRepository;
    private final FoodRepository foodRepository;
    private final TypeSeatRepository typeSeatRepository;

    public PriceDetailServiceImpl(PriceDetailRepository priceDetailRepository, ModelMapper modelMapper, PriceHeaderRepository priceHeaderRepository, FoodRepository foodRepository, TypeSeatRepository typeSeatRepository) {
        this.priceDetailRepository = priceDetailRepository;
        this.modelMapper = modelMapper;
        this.priceHeaderRepository = priceHeaderRepository;
        this.foodRepository = foodRepository;
        this.typeSeatRepository = typeSeatRepository;

    }


    @Override
    public void createPriceDetail(Set<PriceDetailDto> priceDetailDtos) {
        for (PriceDetailDto priceDetailDto : priceDetailDtos) {
            PriceDetail priceDetail = modelMapper.map(priceDetailDto, PriceDetail.class);
            PriceHeader priceHeader = priceHeaderRepository.findById(priceDetailDto.getPriceHeaderId())
                    .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với id: "+ priceDetailDto.getPriceHeaderId(), HttpStatus.NOT_FOUND));
            if (priceDetailDto.getTypeSeatId() != null && priceDetailDto.getTypeSeatId() > 0) {
                boolean typeSeatExists = priceHeader.getPriceDetails().stream()
                        .anyMatch(detail -> detail.getTypeSeat() != null && detail.getTypeSeat().getId().equals(priceDetailDto.getTypeSeatId()));
                if (typeSeatExists) {
                    throw new AppException("Loại ghế : " + priceHeader.getName()+" đã tồn tại trong chương trình thay đổi giá này.", HttpStatus.BAD_REQUEST);
                }
                TypeSeat typeSeat = typeSeatRepository.findById(priceDetailDto.getTypeSeatId())
                        .orElseThrow(() -> new AppException("Không tìm thấy loại ghế với id: "+ priceDetailDto.getId(), HttpStatus.NOT_FOUND));
                priceDetail.setTypeSeat(typeSeat);
                if (priceDetailDto.getPrice() > 0) {
                    priceDetail.setPrice(priceDetailDto.getPrice());
                } else {
                    throw new AppException("Giá mới phải lớn hơn 0", HttpStatus.BAD_REQUEST);
                }
            } else if (priceDetailDto.getFoodId() != null && priceDetailDto.getFoodId() > 0) {
                boolean foodExists = priceHeader.getPriceDetails().stream()
                        .anyMatch(detail -> detail.getFood() != null && detail.getFood().getId().equals(priceDetailDto.getFoodId()));
                if (foodExists) {
                    throw new AppException("Đồ ăn  đã tồn tại trong chương trình thay đổi giá này.", HttpStatus.BAD_REQUEST);
                }
                Food food = foodRepository.findById(priceDetailDto.getFoodId())
                            .orElseThrow(() -> new AppException("Không tìm thấy đồ ăn với id: "+ priceDetailDto.getId(), HttpStatus.NOT_FOUND));
                priceDetail.setFood(food);
                if (priceDetailDto.getPrice() > 0) {
                    priceDetail.setPrice(priceDetailDto.getPrice());
                } else {
                    throw new AppException("Giá mới phải lớn hơn 0", HttpStatus.BAD_REQUEST);
                }
            } else {
                throw new AppException("Loại ghế hoặc đồ ăn không được để trống", HttpStatus.BAD_REQUEST);
            }
            priceDetail.setCreatedDate(LocalDateTime.now());
            if(!priceHeader.isStatus()){
                priceDetail.setStatus(false);
            }
            priceDetail.setPriceHeader(priceHeader);
            priceHeader.getPriceDetails().add(priceDetail);
            priceDetailRepository.save(priceDetail);
        }
    }


    @Override
    public PriceDetailDto getPriceDetail(Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: "+ id, HttpStatus.NOT_FOUND));
        return modelMapper.map(priceDetail, PriceDetailDto.class);
    }

    @Override
    public void updatePriceDetail(PriceDetailDto priceDetailDto) {
        PriceDetail priceDetail = priceDetailRepository.findById(priceDetailDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: "+ priceDetailDto.getId(), HttpStatus.NOT_FOUND));
        //nếu ngày bắt đầu hoặc ngày kết thúc của header đã  qua thì không thể update giá
        if (priceDetail.getPriceHeader().getStartDate().isAfter(LocalDateTime.now()) ||
                priceDetail.getPriceHeader().getEndDate().isAfter(LocalDateTime.now())) {
            throw new AppException("Chương trình đã bắt đầu, không thể cập nhật", HttpStatus.BAD_REQUEST);
        }
        if (priceDetailDto.getPrice() < 0) {
            throw new AppException("Giá mới phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }
        priceDetail.setPrice(priceDetailDto.getPrice());
        if(priceDetailDto.isStatus() != priceDetail.isStatus()){
            if(priceDetailDto.isStatus() && !priceDetail.getPriceHeader().isStatus()){
                throw new AppException("Không thể kích hoạt chi tiết chương trình khi chương trình thay đổi giá chưa được kích hoạt", HttpStatus.BAD_REQUEST);
            }else {
                priceDetail.setStatus(priceDetailDto.isStatus());
            }
        }else {
            priceDetail.setStatus(priceDetail.isStatus());
        }

        priceDetailRepository.save(priceDetail);

    }

    @Override
    public void deletePriceDetail(Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: "+ id, HttpStatus.NOT_FOUND));
        //nếu ngày bắt đầu và kết thúc của Priceheader đã qua ngày hiện tại thì không được update
        if(priceDetail.getPriceHeader().getStartDate().isAfter(LocalDateTime.now()) ||
                priceDetail.getPriceHeader().getEndDate().isAfter(LocalDateTime.now())){
            throw new AppException("Chương trình đã bắt đầu, không thể xóa", HttpStatus.BAD_REQUEST);
        }
        priceDetailRepository.delete(priceDetail);



    }

    @Override
    public Set<PriceDetailDto> getAllPriceDetail(Long id) {
        PriceHeader priceHeader = priceHeaderRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với id: "+ id, HttpStatus.NOT_FOUND));
        return modelMapper.map(priceHeader.getPriceDetails(), Set.class);
    }


}
