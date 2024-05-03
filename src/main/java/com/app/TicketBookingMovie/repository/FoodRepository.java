package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Food;
import com.app.TicketBookingMovie.models.enums.ESize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends JpaRepository<Food, Long> {


    Optional<Food> findByName(String name);

    long countByCinemaIdAndCodeContaining(Long cinemaId, String code);

    long countByCinemaIdAndNameContaining(Long cinemaId, String name);

    long countByCinemaIdAndCategoryFoodId(Long cinemaId, Long categoryId);

    long countByCinemaIdAndSize(Long cinemaId, ESize eSize);

    long countByCinemaId(Long cinemaId);

    Optional<Object> findByNameAndCinemaId(String name, Long cinemaId);

    Optional<Food> findByIdAndCinemaId(Long foodId, Long cinemaId);
}
