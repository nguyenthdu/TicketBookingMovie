package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.CategoryFood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CateogryFoodRepository extends JpaRepository<CategoryFood, Long> {
    Optional<CategoryFood> findByName(String name);

    long countByNameContaining(String name);

    long countByCodeContaining(String code);
}
