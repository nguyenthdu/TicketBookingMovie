package com.app.TicketBookingMovie.payload.response;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String code;
    private String username;
    private String email;
    private List<String> roles;
    private LocalDate birthday;
    private String phone;
    private boolean enabled;
    private boolean gender;
    private LocalDateTime createdDate;

    public JwtResponse(String accessToken, Long id, String code, String username, String email, List<String> roles, LocalDate birthday, String phone, boolean enabled, boolean gender, LocalDateTime createdDate) {
        this.token = accessToken;
        this.id = id;
        this.code = code;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.birthday = birthday;
        this.phone = phone;
        this.enabled = enabled;
        this.gender = gender;
        this.createdDate = createdDate;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}