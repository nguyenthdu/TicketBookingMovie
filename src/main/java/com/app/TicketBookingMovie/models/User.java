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
        name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email"),
        @UniqueConstraint(columnNames = "phone"),
        @UniqueConstraint(columnNames = "code")}
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    @NotEmpty(message = "Username is not empty")
    @Size(min = 3, max = 32, message = "Username is not between 3 and 20 characters")
    private String username;
    @NotEmpty(message = "Email is not empty")
    @Email(message = "Email is not valid")
    private String email;
    @NotNull
    private boolean gender;
    @NotNull
    @Past(message = "Birthday is not valid")
    private LocalDate birthday;
    @NotEmpty(message = "Phone is not empty")
    private String phone;
    @NotEmpty(message = "Password is not empty")
    private String password;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    private boolean enabled = true;
    private LocalDate createdDate = LocalDate.now();

    public User(String username, String email, boolean gender, LocalDate birthday, String phone, String password) {
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