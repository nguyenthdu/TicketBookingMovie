package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.SalePriceDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalePriceDetailRepository extends JpaRepository<SalePriceDetail, Long> {


    SalePriceDetail findByTypeSeatIdAndFoodId(Long typeSeatId, Long foodId);


}
