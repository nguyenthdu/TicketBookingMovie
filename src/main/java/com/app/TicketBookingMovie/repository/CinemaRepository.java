package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {


    Optional<Cinema> findByName(String name);

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByAddressStreet(String street);

    long countByAddressDistrict(String district);

    long countByAddressCity(String city);

    long countByAddressNation(String nation);

    long countByAddressWard(String ward);
}
