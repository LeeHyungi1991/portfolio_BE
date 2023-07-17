package com.portfolio.configs;

import com.portfolio.models.entity.User;
import com.portfolio.service.DataBaseService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtConfig {
//    public static HashSet<String> refreshDBToken = new HashSet<>();
//    public static HashSet<String> accessDBToken = new HashSet<>();
//    public static Map<String, String[]> loginDBUsers = new HashMap<>();

    @Value("${spring.jwt.refresh.expire.time}")
    private long refreshTokenExpireTime;

    @Value("${spring.jwt.access.expire.time}")
    private long accessTokenExpireTime;

    @Value("${spring.jwt.secret.access}")
    private String accessSecretKey;

    @Value("${spring.jwt.algorithm.access}")
    private String accessAlgorithm;

    @Value("${spring.jwt.secret.refresh}")
    private String refreshSecretKey;

    @Value("${spring.jwt.algorithm.refresh}")
    private String refreshAlgorithm;

    private final CustomUserDetailsService userDetailsService;
    private final DataBaseService dataBaseService;

    @PostConstruct
    protected void init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.getBytes());
    }

    public String createAccessToken(String userName, String name, List<String> roleList) {
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put("roles", roleList);
        claims.put("name", name);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpireTime))
                .signWith(SignatureAlgorithm.valueOf(accessAlgorithm), accessSecretKey)
                .setHeaderParam("typ", "JWT")
                .compact();
    }

    public String createRefreshToken(String userName, String name, List<String> roleList) {
        Claims claims = Jwts.claims().setSubject(userName);
        claims.put("roles", roleList);
        claims.put("name", name);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpireTime))
                .signWith(SignatureAlgorithm.valueOf(refreshAlgorithm), refreshSecretKey)
                .setHeaderParam("typ", "JWT")
                .compact();
    }

    public Authentication getAuthentication(String token, boolean isRefresh) {
        Jws<Claims> jws = Jwts.parser().setSigningKey(isRefresh ? refreshSecretKey : accessSecretKey).parseClaimsJws(token);
        User user = userDetailsService.loadUserByUsername(jws.getBody().getSubject());
        return new UsernamePasswordAuthenticationToken(user, "", user.getAuthorities());
    }

    // Request의 Header에서 token 값을 가져옵니다. "Authorization" : "TOKEN값'
    public String resolveAccessToken(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null) {
            return null;
        }
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        if (request.getAttribute("newAuthorization") != null) {
            token = ((String) request.getAttribute("newAuthorization")).replace("Bearer ", "");
            String email = getAuthentication(token, false).getName();
            dataBaseService.saveAccessToken(email, token);
        }
        return token.replace("Bearer ", "");
    }

    // 토큰의 유효성 + 만료일자 확인
    public boolean validateAccessToken(String accessToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(accessSecretKey).parseClaimsJws(accessToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        if (request.getHeader("cookie") == null) {
            return null;
        }
        String[] split = request.getHeader("cookie").split(";");
        for (String s : split) {
            if (s.contains("refreshToken")) {
                return s.replace("refreshToken=", "").trim();
            }
        }
        return null;
    }

    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(refreshSecretKey).parseClaimsJws(refreshToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}