package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);


    long countByCodeContaining(String code);

    long countByNameContaining(String name);

    long countByCinemaId(Long cinemaId);
}
