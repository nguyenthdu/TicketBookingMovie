package com.app.TicketBookingMovie.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
		name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"), @UniqueConstraint(columnNames = "email")}
)
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Size(max = 10)
	private Integer code;
	@NotBlank(message = "Username is not blank")
	@Size(min = 3, max = 20, message = "Username is not between 3 and 20 characters")
	private String username;
	@NotBlank(message = "Email is not blank")
	@Size(max = 50, message = "Email is not more than 50 characters")
	@Email(message = "Email is not valid")
	private String email;
	private boolean gender;
	@NotNull(message = "Birthday is not null")
	@Past(message = "Birthday is not valid")
	private LocalDate birthday;
	@NotBlank(message = "Phone is not blank")
	@Size(max = 10, message = "Phone is not more than 10 characters")
	private String phone;
	@NotBlank(message = "Password is not blank")
	private String password;
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();
	private boolean enabled = true;
	@Column(name = "created_date")
	private LocalDate createdDate = LocalDate.now();
	
	public User(Integer code, String username, String email, boolean gender, LocalDate birthday, String phone, String password) {
		this.code = code;
		this.username = username;
		this.email = email;
		this.gender = gender;
		this.birthday = birthday;
		this.phone = phone;
		this.password = password;
	}
	
	public User() {
	}
}