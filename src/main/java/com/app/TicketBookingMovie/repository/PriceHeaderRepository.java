package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.PriceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PriceHeaderRepository extends JpaRepository<PriceHeader, Long> {
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate);

    Page<PriceHeader> findByCodeContaining(String code, Pageable pageable);

    Page<PriceHeader> findByNameContaining(String name, Pageable pageable);
    long countByCodeContaining(String code);

    long countByNameContaining(String name);


    long countByStartDateGreaterThanEqualAndEndDateLessThanEqual(LocalDateTime start, LocalDateTime end);
    Page<PriceHeader> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDateTime endDate, LocalDateTime startDate, Pageable pageable);
    Page<PriceHeader> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE PriceHeader ph SET ph.status = true WHERE ph.startDate <= :currentTime AND ph.status = false")
    void updatePriceHeadersStatus(LocalDateTime currentTime);

    @Transactional
    @Modifying
    @Query("UPDATE PriceDetail pd SET pd.status = true WHERE pd.priceHeader.startDate <= :currentTime AND pd.status = false")
    void updatePriceDetailsStatus(LocalDateTime currentTime);

    boolean existsByStatus(boolean b);
}
