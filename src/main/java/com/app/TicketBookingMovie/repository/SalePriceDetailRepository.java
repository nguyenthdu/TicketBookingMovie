package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.SalePriceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SalePriceDetailRepository extends JpaRepository<SalePriceDetail, Long> {


@Query("SELECT s FROM SalePriceDetail s WHERE s.food.id = ?1")
    Object findByFoodId(Long foodId);
@Query("SELECT s FROM SalePriceDetail s WHERE s.typeSeat.id = ?1")
    Object findByTypeSeatId(Long typeSeatId);
}
