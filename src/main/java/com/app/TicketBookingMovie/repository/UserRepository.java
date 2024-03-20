package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    Page<User> findByPhoneContaining(String phone, Pageable pageable);

    Page<User> findByUsernameContaining(String username, Pageable pageable);

    Page<User> findByEmailContaining(String email, Pageable pageable);

    Page<User> findByCodeContaining(String code, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = ?1")
    Page<User> findByRoleId(Long roleId, Pageable pageable);

    long countByCodeContaining(String code);

    long countByUsernameContaining(String username);

    long countByPhoneContaining(String phone);

    long countByEmailContaining(String email);

    long countByRoleId(Long roleId);
}