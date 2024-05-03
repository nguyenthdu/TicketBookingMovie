package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    long countAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(LocalDateTime startDate, LocalDateTime endDate, boolean status);

    long countAllByStartDateGreaterThanEqualAndStatus(LocalDateTime startDate, boolean status);

    long countAllByEndDateLessThanEqualAndStatus(LocalDateTime endDate, boolean status);

    long countAllByStatus(boolean status);

    @Transactional
    @Modifying
    @Query("UPDATE Promotion p SET p.status = true WHERE p.startDate <= :currentTime AND p.status = false")
    void updatePromotionStatus(LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("UPDATE Promotion p SET p.status = false WHERE p.endDate <= :currentTime AND p.status = true")
    void updatePromotionLineStatus(LocalDateTime currentTime);
}
