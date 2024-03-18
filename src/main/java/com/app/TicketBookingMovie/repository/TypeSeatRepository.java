package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.TypeSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeSeatRepository extends JpaRepository<TypeSeat, Long> {
    //tạo ghế mặc định với type  và giá

}
