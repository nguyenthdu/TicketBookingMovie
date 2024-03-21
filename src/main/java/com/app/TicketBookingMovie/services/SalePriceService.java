package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.SalePriceDto;

import java.time.LocalDateTime;
import java.util.List;

public interface SalePriceService {
    void createSalePrice(SalePriceDto salePriceDto);

    //name description  status and endDate
    void updateSalePrice(SalePriceDto salePriceDto);

    SalePriceDto getSalePriceById(Long id);

    void deleteSalePriceById(Long id);

    List<SalePriceDto> getAllSalePrice(Integer page, Integer size, String code, String name, boolean status, LocalDateTime startDate, LocalDateTime endDate);

    long countAllSalePrice(String code, String name, boolean status, LocalDateTime startDate, LocalDateTime endDate);

}
