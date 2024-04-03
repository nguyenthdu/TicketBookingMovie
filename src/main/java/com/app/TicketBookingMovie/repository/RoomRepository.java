package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);

    Page<Room> findByCodeContaining(String code, Pageable pageable);

    Page<Room> findByNameContaining(String name, Pageable pageable);

    Page<Room> findByCinemaId(Long cinemaId, Pageable pageable);
    List<Room> findByCinemaId(Long cinemaId);

    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByCinemaId(Long cinemaId);
}
