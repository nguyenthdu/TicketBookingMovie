package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.CategoryFood;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CateogryFoodRepository extends JpaRepository<CategoryFood, Long> {
    Optional<CategoryFood> findByName(String name);

    Page<CategoryFood> findByNameContaining(String name, Pageable pageable);

    long countByNameContaining(String name);

    Page<CategoryFood> findByCodeContaining(String code, Pageable pageable);

    long countByCodeContaining(String code);
}
