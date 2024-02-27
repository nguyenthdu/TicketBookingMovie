package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ERole;
import com.app.TicketBookingMovie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User> findByCode(Integer code);
	Optional<User> findByPhone(String phone);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByEmail(String email);
	 List<User> findAllByRolesName(ERole roleName);
	 

   
}