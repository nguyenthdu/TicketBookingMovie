package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ShowTimeDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ShowTimeService {
    Set<ShowTimeDto> createShowTime(Set<ShowTimeDto> showTimeDtos);
    ShowTimeDto getShowTimeById(Long id);
    List<ShowTimeDto> getAllShowTimes(Integer page, Integer size,String code, Long movieId, LocalDate date);

    void updateShowTimeStatus();
    long countAllShowTimes(String code,Long movieId, LocalDate date);

}
