package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.PriceDetailDto;
import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.PriceDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface PriceDetailService {
    void createPriceDetail(Set<PriceDetailDto> priceDetailDto);
    PriceDetailDto getPriceDetail(Long id);
    PriceDetail getPriceDetailByFood(Food food);
    void updatePriceDetail(BigDecimal price, boolean status, Long id);
    void deletePriceDetail(Long id);
    List<PriceDetail> priceActive();
    List<PriceDetailDto> getAllPriceDetail(Integer page, Integer size, Long priceHeaderId,String typeDetail,String  foodCode, String  roomCode , String  typeSeatCode);
    long countAllPriceDetail(Long priceHeaderId,String typeDetail,String  foodCode, String  roomCode , String  typeSeatCode);
}
