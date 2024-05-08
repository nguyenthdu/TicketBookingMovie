package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ShowTime;
import com.app.TicketBookingMovie.models.ShowTimeSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ShowTimeSeatRepository extends JpaRepository<ShowTimeSeat, Long> {
    void deleteAllByShowTime(ShowTime showTime);

    List<ShowTimeSeat> findByShowTimeIdAndSeatIdIn(Long showTimeId, Set<Long> seatIds);
}
