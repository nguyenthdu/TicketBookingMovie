package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.repository.PriceDetailRepository;
import com.app.TicketBookingMovie.repository.PriceHeaderRepository;
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


    public PriceDetailServiceImpl(PriceDetailRepository priceDetailRepository, ModelMapper modelMapper, PriceHeaderRepository priceHeaderRepository) {
        this.priceDetailRepository = priceDetailRepository;
        this.modelMapper = modelMapper;
        this.priceHeaderRepository = priceHeaderRepository;


    }


    @Override
    public PriceDetail createPriceDetail(PriceDetailDto priceDetailDto) {
        PriceHeader priceHeader = priceHeaderRepository.findById(priceDetailDto.getPriceHeaderId())
                .orElseThrow(() -> new AppException("Không tìm thấy chương trình thay đổi giá với id: "+ priceDetailDto.getPriceHeaderId(), HttpStatus.NOT_FOUND));
        if(!priceHeader.getEndDate().isAfter(LocalDateTime.now())){
            throw new AppException("Chương trình đã kết thúc, không thể thêm chi tiết chương trình", HttpStatus.BAD_REQUEST);
        }
        if(priceDetailDto.getPrice() < 0){
            throw new AppException("Giá mới phải lớn hơn 0", HttpStatus.BAD_REQUEST);
        }
        PriceDetail priceDetail = modelMapper.map(priceDetailDto, PriceDetail.class);
        priceDetail.setPriceHeader(priceHeader);
        priceDetail.setStatus(priceDetailDto.isStatus());
        priceDetail.setCreatedDate(LocalDateTime.now());
        priceDetailRepository.save(priceDetail);
        return priceDetail;
    }


    @Override
    public PriceDetailDto getPriceDetail(Long id) {
        PriceDetail priceDetail = priceDetailRepository.findById(id)
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: "+ id, HttpStatus.NOT_FOUND));
        return modelMapper.map(priceDetail, PriceDetailDto.class);
    }

    @Override
    public PriceDetail updatePriceDetail(PriceDetailDto priceDetailDto) {
        PriceDetail priceDetail = priceDetailRepository.findById(priceDetailDto.getId())
                .orElseThrow(() -> new AppException("Không tìm thấy chi tiết chương trình thay đổi giá với id: "+ priceDetailDto.getId(), HttpStatus.NOT_FOUND));
        //nếu ngày bắt đầu hoặc ngày kết thúc của header đã  qua thì không thể update giá
        if (!priceDetail.getPriceHeader().getEndDate().isBefore(LocalDateTime.now())){
            throw new AppException("Chương trình đã kết thúc, không thể cập nhật chi tiết chương trình", HttpStatus.BAD_REQUEST);
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
        return priceDetail;

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
