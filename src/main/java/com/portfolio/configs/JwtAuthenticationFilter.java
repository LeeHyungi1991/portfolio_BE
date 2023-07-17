package com.portfolio.configs;

import com.portfolio.models.entity.User;
import com.portfolio.service.DataBaseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.security.auth.login.CredentialException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtConfig jwtTokenProvider;
    private final DataBaseService dataBaseService;
    private Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String accessToken = jwtTokenProvider.resolveAccessToken((HttpServletRequest) request);
        try {
            if (accessToken != null && dataBaseService.matchAccessToken(accessToken)) {
                if (!jwtTokenProvider.validateAccessToken(accessToken)) {
                    String refreshToken = jwtTokenProvider.resolveRefreshToken((HttpServletRequest) request);
                    if (refreshToken != null && dataBaseService.matchRefreshToken(refreshToken) && jwtTokenProvider.validateRefreshToken(refreshToken)) {
                        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken, true);
                        User user = authentication.getPrincipal() instanceof User ? (User) authentication.getPrincipal() : null;
                        accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getName(), user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
                        dataBaseService.saveAccessToken(user.getEmail(), accessToken).orElseThrow(() -> new CredentialException("해당 사용자가 존재하지 않습니다."));
                        request.setAttribute("newAuthorization", "Bearer " + accessToken);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken, false);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        chain.doFilter(request, response);
    }
}