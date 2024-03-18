package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>{
}
