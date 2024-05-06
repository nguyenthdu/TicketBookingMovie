package com.app.TicketBookingMovie.security;

import com.app.TicketBookingMovie.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String code;
    private String username;
    private String email;
    private LocalDate birthday;
    private String phone;
    private boolean enabled;
    private boolean gender;
    private LocalDateTime createdDate;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String code, String username, String email,
                           LocalDate birthday, String phone, boolean enabled, boolean gender, LocalDateTime createdDate,
                           String password,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
		this.code = code;
        this.username = username;
        this.email = email;
        this.birthday = birthday;
        this.phone = phone;
        this.enabled = enabled;
        this.gender = gender;
        this.createdDate = createdDate;
        this.password = password;
        this.authorities = authorities;


    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getCode(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthday(),
                user.getPhone(),
                user.isEnabled(),
                user.isGender(),
                user.getCreatedDate(),
                user.getPassword(),
                authorities);

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isGender() {
        return gender;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getCode() {
        return code;
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}