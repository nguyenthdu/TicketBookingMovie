package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.enums.ESize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {
    Page<Food> findAllByCodeContaining(String code, Pageable pageable);

    Page<Food> findAllByNameContaining(String name, Pageable pageable);

    Page<Food> findAllByCategoryFoodId(Long categoryId, Pageable pageable);

    Page<Food> findAllBySize(ESize eSize, Pageable pageable);

    long countAllByCodeContaining(String code);

    long countAllByNameContaining(String name);

    long countAllByCategoryFoodId(Long categoryId);

    long countAllBySize(ESize eSize);

    Optional<Food> findByName(String name);
}
