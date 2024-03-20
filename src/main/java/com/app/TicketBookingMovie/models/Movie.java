package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "movie",
		uniqueConstraints = {
				@UniqueConstraint(columnNames = "code")
		}
)
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String code;
	@NotEmpty
	private String name;
	@NotEmpty
	private String imageLink;
	@NotEmpty
	private String trailerLink;
	@NotEmpty
	private String description;
	@NotEmpty
	private int durationMinutes;
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "movie_genre",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private Set<Genre> genres;
	//many to many cinema
	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
	@JoinTable(name = "movie_cinema",
			joinColumns = @JoinColumn(name = "movie_id"),
			inverseJoinColumns = @JoinColumn(name = "cinema_id"))
	private Set<Cinema> cinemas;
	@NotEmpty
	private LocalDate releaseDate;
	@NotEmpty
	private String country;
	@NotEmpty
	private String director;
	@NotEmpty
	private String cast;
	@NotEmpty
	private String producer;
	@OneToMany
	private Set<ShowTime> showTimes;
	public Movie() {
	}

}
