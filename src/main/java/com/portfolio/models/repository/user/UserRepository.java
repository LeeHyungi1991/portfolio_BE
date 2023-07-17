package com.portfolio.models.repository.user;

import com.portfolio.models.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findByEmail(String email);
    boolean findByRefreshToken(String refreshToken);
    boolean findByAccessToken(String accessToken);
}
