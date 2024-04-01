package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PriceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PriceHeaderRepository extends JpaRepository<PriceHeader, Long> {
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate);

    Page<PriceHeader> findByCodeContaining(String code, Pageable pageable);

    Page<PriceHeader> findByNameContaining(String name, Pageable pageable);

    Page<PriceHeader> findByStatus(boolean status, Pageable pageable);


    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByStatus(boolean status);

    long countByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime start, LocalDateTime end);

    Page<PriceHeader> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate, Pageable pageable);
}
