package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.SalePriceDetailDto;

import java.util.Set;

public interface SalePriceDetailService {
    void createSalePriceDetail(Set<SalePriceDetailDto> salePriceDetailDto);
    SalePriceDetailDto getSalePriceDetail(Long id);
    void updateStatusSalePriceDetail(Long id);
    void deleteSalePriceDetail(Long id);
    Set<SalePriceDetailDto> getAllSalePriceDetail(Long id);

}
