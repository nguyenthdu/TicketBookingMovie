package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ERole;
import com.app.TicketBookingMovie.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(ERole name);
}