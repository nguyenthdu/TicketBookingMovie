package com.app.TicketBookingMovie.repository;

import com.app.TicketBookingMovie.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository  extends JpaRepository<Address, Long>{
}
