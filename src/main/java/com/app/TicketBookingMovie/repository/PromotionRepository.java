package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long>{
    Page<Promotion> findAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(LocalDateTime startDate, LocalDateTime endDate, boolean status, Pageable pageable);

    Page<Promotion> findAllByStartDateGreaterThanEqualAndStatus(LocalDateTime startDate, boolean status, Pageable pageable);

    Page<Promotion> findAllByEndDateLessThanEqualAndStatus(LocalDateTime endDate, boolean status, Pageable pageable);

    Page<Promotion> findAllByStatus(boolean status, Pageable pageable);

    long countAllByStartDateGreaterThanEqualAndEndDateLessThanEqualAndStatus(LocalDateTime startDate, LocalDateTime endDate, boolean status);

    long countAllByStartDateGreaterThanEqualAndStatus(LocalDateTime startDate, boolean status);

    long countAllByEndDateLessThanEqualAndStatus(LocalDateTime endDate, boolean status);

    long countAllByStatus(boolean status);
}
