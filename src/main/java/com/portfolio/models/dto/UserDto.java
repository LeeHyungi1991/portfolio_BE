package com.portfolio.models.dto;

import com.portfolio.models.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class UserDto {
    public UserDto(User user) {
        this.seq = user.getSeq();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.roles = user.getRoles();
        this.createAt = user.getCreateAt();
        this.lastLoginAt = user.getLastLoginAt();
        this.accessToken = user.getAccessToken();
        this.refreshToken = user.getRefreshToken();
    }
    private Long seq;
    private String name;
    private String email;
    private String password;
    private String roles;
    private Timestamp createAt;
    private Timestamp lastLoginAt;
    private String accessToken;
    private String refreshToken;
    public String getUsername() {
        return this.email;
    }

    public List<String> getAuthorities() {
        List<String> authorities = new ArrayList<>();
        authorities.add(roles);
        return authorities;
    }
}
