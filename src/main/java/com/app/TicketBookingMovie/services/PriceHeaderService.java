package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PriceHeaderDto;
import com.app.TicketBookingMovie.models.PriceHeader;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceHeaderService {
    void createPriceHeader(PriceHeaderDto priceHeaderDto);

    //name description  status and endDate
    void updatePriceHeader(PriceHeaderDto priceHeaderDto);

    PriceHeaderDto getPriceHeaderById(Long id);
    PriceHeader findById(Long id);
    void deletePriceHeaderById(Long id);

    List<PriceHeaderDto> getAllPriceHeader(Integer page, Integer size, String code, String name, LocalDateTime startDate, LocalDateTime endDate);

    long countAllPriceHeader(String code, String name,  LocalDateTime startDate, LocalDateTime endDate);

}
