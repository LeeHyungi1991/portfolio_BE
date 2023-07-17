package com.portfolio.models.repository.user;

import com.portfolio.models.entity.User;
import com.portfolio.models.repository.CommonRepositoryImpl;

import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl extends CommonRepositoryImpl<User, Long> implements UserRepository {
    @Override
    public Optional<User> findByEmail(String email) {
        List<User> results =  createTypedQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", email).getResultList();
        return results.stream().findAny();
    }

    @Override
    public boolean findByRefreshToken(String refreshToken) {
        List<User> users =  createTypedQuery("select u from User u where u.refreshToken = :refreshToken", User.class)
                .setParameter("refreshToken", refreshToken)
                .getResultList();
        return users.size() > 0;
    }

    @Override
    public boolean findByAccessToken(String accessToken) {
        List<User> users = createTypedQuery("select u from User u where u.accessToken = :accessToken", User.class)
                .setParameter("accessToken", accessToken)
                .getResultList();
        return users.size() > 0;
    }
}
