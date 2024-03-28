package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.SalePriceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SalePriceDetailRepository extends JpaRepository<SalePriceDetail, Long> {


    @Query("SELECT spd FROM SalePriceDetail spd " +
            "JOIN spd.salePrice sp " +
            "WHERE sp.status = true " +
            "AND sp.startDate < :currentTime AND sp.endDate > :currentTime")
    List<SalePriceDetail> findCurrentSalePriceDetails(@Param("currentTime") LocalDateTime currentTime);

}
