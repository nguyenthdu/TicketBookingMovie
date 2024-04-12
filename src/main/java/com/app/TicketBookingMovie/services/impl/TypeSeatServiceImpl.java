package com.app.TicketBookingMovie.services.impl;

import com.app.TicketBookingMovie.dtos.TypeSeatDto;
import com.app.TicketBookingMovie.exception.AppException;
import com.app.TicketBookingMovie.models.TypeSeat;
import com.app.TicketBookingMovie.models.enums.ETypeSeat;
import com.app.TicketBookingMovie.repository.TypeSeatRepository;
import com.app.TicketBookingMovie.services.TypeSeatService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TypeSeatServiceImpl implements TypeSeatService {
    private final ModelMapper modelMapper;
    private final TypeSeatRepository typeSeatRepository;

    public TypeSeatServiceImpl(ModelMapper modelMapper, TypeSeatRepository typeSeatRepository) {
        this.modelMapper = modelMapper;
        this.typeSeatRepository = typeSeatRepository;
    }

    @Override
    public void createTypeSeat() {
        if (!typeSeatRepository.findAll().isEmpty()) {
            return;
        }
        Set<TypeSeat> listTypeSeat = new HashSet<>();
        TypeSeat TypeSeatStandard = new TypeSeat();
        TypeSeatStandard.setCode("LG111111111");
        TypeSeatStandard.setName(ETypeSeat.STANDARD);
        TypeSeat typeSeatVip = new TypeSeat();
        typeSeatVip.setCode("LG222222222");
        typeSeatVip.setName(ETypeSeat.VIP);
        TypeSeat typeSeatSweetBox = new TypeSeat();
        typeSeatSweetBox.setCode("LG333333333");
        typeSeatSweetBox.setName(ETypeSeat.SWEETBOX);
        listTypeSeat.add(TypeSeatStandard);
        listTypeSeat.add(typeSeatVip);
        listTypeSeat.add(typeSeatSweetBox);
        typeSeatRepository.saveAll(listTypeSeat);
    }

    @Override
    public TypeSeatDto getTypeSeatById(Long id) {
        TypeSeat typeSeat = typeSeatRepository.findById(id)
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + id, HttpStatus.NOT_FOUND));
        TypeSeatDto typeSeatDto = new TypeSeatDto();
        typeSeatDto.setId(typeSeat.getId());
        typeSeatDto.setCode(typeSeat.getCode());
        typeSeatDto.setName(String.valueOf(typeSeat.getName()));
        typeSeat.getPriceDetails().stream().findFirst().ifPresent(priceDetail -> typeSeatDto.setPrice(priceDetail.getPrice()));
        typeSeat.getPriceDetails().stream().findFirst().ifPresent(priceDetail -> typeSeatDto.setActive_price(priceDetail.isStatus()));
        return typeSeatDto;


    }

    @Override
    public TypeSeat findById(Long id) {
        return typeSeatRepository.findById(id)
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + id, HttpStatus.NOT_FOUND));
    }

    @Override
    public void updateTypeSeatById(TypeSeatDto typeSeatDto) {
        TypeSeat typeSeat = typeSeatRepository.findById(typeSeatDto.getId())
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + typeSeatDto.getId(), HttpStatus.NOT_FOUND));
        TypeSeat updatedTypeSeat = typeSeatRepository.save(typeSeat);
        modelMapper.map(updatedTypeSeat, TypeSeatDto.class);

    }


    @Override
    public Set<TypeSeatDto> getAllTypeSeats() {
        return typeSeatRepository.findAll().stream()
                .map(typeSeat -> {
                    TypeSeatDto typeSeatDto = new TypeSeatDto();
                    typeSeatDto.setId(typeSeat.getId());
                    typeSeatDto.setCode(typeSeat.getCode());
                    typeSeatDto.setName(String.valueOf(typeSeat.getName()));
                    typeSeat.getPriceDetails().stream().findFirst().ifPresent(priceDetail -> typeSeatDto.setPrice(priceDetail.getPrice()));
                    typeSeat.getPriceDetails().stream().findFirst().ifPresent(priceDetail -> typeSeatDto.setActive_price(priceDetail.isStatus()));
                    return typeSeatDto;
                }).collect(Collectors.toSet());


    }


}
