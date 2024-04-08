package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.PriceDetail;
import com.app.TicketBookingMovie.models.PriceHeader;
import com.app.TicketBookingMovie.models.TypeSeat;
import com.app.TicketBookingMovie.repository.TypeSeatRepository;
import com.app.TicketBookingMovie.services.PriceDetailService;
import com.app.TicketBookingMovie.services.PriceHeaderService;
import com.app.TicketBookingMovie.services.TypeSeatService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TypeSeatServiceImpl implements TypeSeatService {
    private final ModelMapper modelMapper;
    private final TypeSeatRepository typeSeatRepository;
    private final PriceDetailService priceDetailService;
    private final PriceHeaderService priceHeaderService;

    public TypeSeatServiceImpl(ModelMapper modelMapper, TypeSeatRepository typeSeatRepository, PriceDetailService priceDetailService, PriceHeaderService priceHeaderService) {
        this.modelMapper = modelMapper;
        this.typeSeatRepository = typeSeatRepository;
        this.priceDetailService = priceDetailService;
        this.priceHeaderService = priceHeaderService;
    }

    @Override
    public void createTypeSeat(TypeSeatDto typeSeatDto, Long priceHeaderId) {
        PriceHeader priceHeader = priceHeaderService.findPriceHeaderById(priceHeaderId);
        PriceDetailDto priceDetailDto = new PriceDetailDto();
        priceDetailDto.setPriceHeaderId(priceHeaderId);
        priceDetailDto.setPrice(typeSeatDto.getPriceDetailDto().getPrice());
        priceDetailDto.setStatus(typeSeatDto.getPriceDetailDto().isStatus());
        PriceDetail priceDetail = modelMapper.map(priceDetailDto, PriceDetail.class);
        priceDetailService.createPriceDetail(priceDetailDto);
        TypeSeat typeSeat = modelMapper.map(typeSeatDto, TypeSeat.class);
        typeSeat.setPrice(priceDetail);
        typeSeatRepository.save(typeSeat);
    }

    @Override
    public TypeSeatDto getTypeSeatById(Long id) {
        return typeSeatRepository.findById(id)
                .map(typeSeat -> modelMapper.map(typeSeat, TypeSeatDto.class))
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + id, HttpStatus.NOT_FOUND));

    }

    @Override
    public void updateTypeSeatById(TypeSeatDto typeSeatDto) {
        TypeSeat typeSeat = typeSeatRepository.findById(typeSeatDto.getId())
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + typeSeatDto.getId(), HttpStatus.NOT_FOUND));
        PriceDetail priceDetail = typeSeat.getPrice();
        PriceDetailDto priceDetailDto = new PriceDetailDto();
        priceDetail.setPrice(typeSeatDto.getPriceDetailDto().getPrice());
        priceDetail.setStatus(typeSeatDto.getPriceDetailDto().isStatus());
        priceDetailService.updatePriceDetail(priceDetailDto);
        typeSeat.setPrice(modelMapper.map(priceDetailDto, PriceDetail.class));
        typeSeatRepository.save(typeSeat);

    }


    @Override
    public Set<TypeSeatDto> getAllTypeSeats() {
        return typeSeatRepository.findAll().stream()
                .map(typeSeat -> modelMapper.map(typeSeat, TypeSeatDto.class))
                .collect(Collectors.toSet());
    }


}
