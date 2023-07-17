package com.portfolio.models.dao;

import com.portfolio.models.entity.User;
import com.portfolio.models.repository.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends UserRepository, JpaRepository<User, Long> {
}
