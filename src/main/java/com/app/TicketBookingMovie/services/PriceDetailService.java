package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;

import java.util.Set;

public interface PriceDetailService {
    void createPriceDetail(Set<PriceDetailDto> priceDetailDto);
    PriceDetailDto getPriceDetail(Long id);
    void updatePriceDetail(PriceDetailDto priceDetailDto);
    void deletePriceDetail(Long id);
    Set<PriceDetailDto> getAllPriceDetail(Long id);

}
