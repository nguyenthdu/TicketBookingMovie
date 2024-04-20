package com.app.TicketBookingMovie.services;

import com.app.TicketBookingMovie.dtos.ShowTimeDto;
import com.app.TicketBookingMovie.dtos.ShowTimeSeatDto;
import com.app.TicketBookingMovie.models.ShowTime;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ShowTimeService {
    void createShowTime(Set<ShowTimeDto> showTimeDtos);

    ShowTimeDto getShowTimeById(Long id);
    ShowTime findById(Long id);

    List<ShowTimeDto> getAllShowTimes(Integer page, Integer size, String code,Long cinemaId, Long movieId, LocalDate date, Long roomId);

    void updateShowTime(ShowTimeDto showTimeDto);

    long countAllShowTimes(String code,Long cinemaId, Long movieId, LocalDate date, Long roomId);
    void deleteShowTime(Long id);
    List<ShowTimeSeatDto> getShowTimeSeatById(Long id);
    void updateSeatStatus(ShowTime showTime);
    Set<LocalDate> getShowDatesByMovieId(Long movieId, Long cinemaId);


}
