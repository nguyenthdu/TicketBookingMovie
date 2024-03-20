package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Seat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.seatType.id = ?1")
    Page<Seat> findBySeatType(Long seatTypeId, Pageable pageable);

}
