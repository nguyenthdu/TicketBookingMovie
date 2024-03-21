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
import java.util.Random;
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
    public Set<TypeSeat> createTypeSeat() {
        if (!typeSeatRepository.findAll().isEmpty()) {
            return new HashSet<>();
        }
        Set<TypeSeat> listTypeSeat = new HashSet<>();
        TypeSeat TypeSeatStandard = new TypeSeat();
        TypeSeatStandard.setCode(randomCode());
        TypeSeatStandard.setName(ETypeSeat.STANDARD);
        TypeSeatStandard.setPrice(0);
        TypeSeat typeSeatVip = new TypeSeat();
        typeSeatVip.setCode(randomCode());
        typeSeatVip.setName(ETypeSeat.VIP);
        typeSeatVip.setPrice(0);
        TypeSeat typeSeatSweetBox = new TypeSeat();
        typeSeatSweetBox.setCode(randomCode());
        typeSeatSweetBox.setName(ETypeSeat.SWEETBOX);
        typeSeatSweetBox.setPrice(0);
        listTypeSeat.add(TypeSeatStandard);
        listTypeSeat.add(typeSeatVip);
        listTypeSeat.add(typeSeatSweetBox);
        typeSeatRepository.saveAll(listTypeSeat);
        return listTypeSeat;

    }

    @Override
    public TypeSeatDto getTypeSeatById(Long id) {
        return typeSeatRepository.findById(id)
                .map(typeSeat -> modelMapper.map(typeSeat, TypeSeatDto.class))
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + id, HttpStatus.NOT_FOUND));

    }

    @Override
    public TypeSeatDto updateTypeSeatById(TypeSeatDto typeSeatDto) {
        TypeSeat typeSeat = typeSeatRepository.findById(typeSeatDto.getId())
                .orElseThrow(() -> new AppException("TypeSeat not found with id: " + typeSeatDto.getId(), HttpStatus.NOT_FOUND));
        if(typeSeatDto.getPrice() > 0){
            typeSeat.setPrice(typeSeatDto.getPrice());
        }else{
            typeSeat.setPrice(typeSeat.getPrice());
        }

        TypeSeat updatedTypeSeat = typeSeatRepository.save(typeSeat);
        return modelMapper.map(updatedTypeSeat, TypeSeatDto.class);
    }



    @Override
    public Set<TypeSeatDto> getAllTypeSeats() {
        return typeSeatRepository.findAll().stream()
                .map(typeSeat -> modelMapper.map(typeSeat, TypeSeatDto.class))
                .collect(Collectors.toSet());
    }

    public String randomCode() {
        Random random = new Random();
        String code;
        int number = random.nextInt(1000);
        code = "LG" + System.currentTimeMillis() + number;
        return code;
    }

}
