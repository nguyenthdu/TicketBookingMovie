package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByPhone(String phone);

    long countByCode(String code);

    long countByUsernameContaining(String username);

    long countByPhone(String phone);

    long countByEmail(String email);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = ?1")
    long countByRoleId(Long roleId);
    User findByEmailIgnoreCase(String emailId);

}