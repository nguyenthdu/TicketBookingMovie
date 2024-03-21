package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.SalePrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SalePriceRepository extends JpaRepository<SalePrice, Long> {
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate);

    Page<SalePrice> findByCodeContaining(String code, Pageable pageable);

    Page<SalePrice> findByNameContaining(String name, Pageable pageable);

    Page<SalePrice> findByStatus(boolean status, Pageable pageable);


    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByStatus(boolean status);

    long countByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime start, LocalDateTime end);

    Page<SalePrice> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate, Pageable pageable);
}
