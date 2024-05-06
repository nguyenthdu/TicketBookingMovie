package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PriceHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PriceHeaderRepository extends JpaRepository<PriceHeader, Long> {
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime start, LocalDateTime end);

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime start, LocalDateTime end);

    @Transactional
    @Modifying
    @Query("UPDATE PriceHeader ph SET ph.status = false WHERE ph.endDate < :currentTime AND ph.status = true")
    void updatePriceHeadersStatus(LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("UPDATE PriceDetail pd SET pd.status = false WHERE pd.priceHeader.status = false AND pd.status = true")
    void updatePriceDetailsStatus(LocalDateTime currentTime);

    boolean existsByStatus(boolean b);
}
