package com.portfolio.configs;

import com.portfolio.models.entity.User;
import com.portfolio.service.DataBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final DataBaseService dataBaseService;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return dataBaseService.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + "는(은) 잘못된 사용자입니다."));
    }
}
