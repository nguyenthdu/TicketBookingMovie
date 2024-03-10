package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.ERole;
import com.app.TicketBookingMovie.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	Optional<User> findByCode(Long code);
	Optional<User> findByPhone(String phone);
	
	Boolean existsByUsername(String username);
	
	Boolean existsByEmail(String email);
	Boolean existsByPhone(String phone);
	 List<User> findAllByRolesName(ERole roleName);
	boolean existsByCode(Long code);
	
	Page<User> findByPhoneContaining(String phone, Pageable pageable);
	
	Page<User> findByCodeContaining(Long code, Pageable pageable);
	
	Page<User> findByEmailContaining(String email, Pageable pageable);
}