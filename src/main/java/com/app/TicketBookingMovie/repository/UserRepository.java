package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.User;
import com.app.TicketBookingMovie.models.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
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

    List<User> findByRoles_NameInOrderByCreatedDateDesc(Collection<ERole> roles_name);
}