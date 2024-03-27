package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>{
}
