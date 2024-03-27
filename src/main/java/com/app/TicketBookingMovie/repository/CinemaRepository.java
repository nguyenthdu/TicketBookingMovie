package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Cinema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CinemaRepository extends JpaRepository<Cinema, Long> {


    Optional<Cinema> findByName(String name);

    @Query("SELECT c FROM Cinema c WHERE c.address.street = :street")
    Page<Cinema> findByAddressStreet(String street, Pageable pageable);

    @Query("SELECT c FROM Cinema c WHERE c.address.ward = :ward")
    Page<Cinema> findByAddressWard(String ward, Pageable pageable);


    @Query("SELECT c FROM Cinema c WHERE c.address.district = :district")
    Page<Cinema> findByAddressDistrict(String district, Pageable pageable);

    @Query("SELECT c FROM Cinema c WHERE c.address.city = :city")
    Page<Cinema> findByAddressCity(String city, Pageable pageable);

    @Query("SELECT c FROM Cinema c WHERE c.address.nation = :nation")
    Page<Cinema> findByAddressNation(String nation, Pageable pageable);


    Page<Cinema> findByNameContaining(String name, Pageable pageable);

    Page<Cinema> findByCodeContaining(String code, Pageable pageable);

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByAddressStreet(String street);

    long countByAddressDistrict(String district);

    long countByAddressCity(String city);

    long countByAddressNation(String nation);

    long countByAddressWard(String ward);
}
