package com.app.TicketBookingMovie.models;

import com.app.TicketBookingMovie.models.enums.ERole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Enumerated(EnumType.STRING)
	private ERole name;
	public Role(ERole name) {
		this.name = name;
	}
	
	public Role() {
	}



}