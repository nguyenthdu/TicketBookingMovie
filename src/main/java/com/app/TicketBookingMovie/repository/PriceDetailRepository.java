package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PriceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PriceDetailRepository extends JpaRepository<PriceDetail, Long> {


    @Query("SELECT spd FROM PriceDetail spd " +
            "JOIN spd.priceHeader sp " +
            "WHERE sp.status = true " +
            "AND sp.startDate < :currentTime AND sp.endDate > :currentTime")
    List<PriceDetail> findCurrentSalePriceDetails(@Param("currentTime") LocalDateTime currentTime);

}
