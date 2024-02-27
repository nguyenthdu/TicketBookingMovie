package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "genre")
public class Genre {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Integer code;
	@Column(nullable = false)
	private String name;
	
	public Genre(Integer code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public Genre() {
	}
}
