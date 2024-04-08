package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.models.PriceDetail;

import java.util.Set;

public interface PriceDetailService {
    PriceDetail createPriceDetail(PriceDetailDto priceDetailDto);
    PriceDetailDto getPriceDetail(Long id);
    PriceDetail updatePriceDetail(PriceDetailDto priceDetailDto);
    void deletePriceDetail(Long id);
    Set<PriceDetailDto> getAllPriceDetail(Long id);

}
