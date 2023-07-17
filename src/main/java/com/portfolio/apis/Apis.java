package com.portfolio.apis;

import com.portfolio.configs.JwtConfig;
import com.portfolio.annotations.NoLogging;
import com.portfolio.handlers.WebsocketHandlerBoard;
import com.portfolio.models.dto.UserDto;
import com.portfolio.models.entity.User;
import com.portfolio.service.DataBaseService;
import com.portfolio.service.GoogleService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.CredentialException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class Apis {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtConfig jwtTokenProvider;
    private final GoogleService googleService;
    private final DataBaseService dataBaseService;
    @Value("${spring.jwt.refresh.expire.time}")
    private long refreshTokenExpireTime;

    @GetMapping("/ping")
    public Map<String, Object> ping(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String accessToken = jwtTokenProvider.resolveAccessToken(request);
            UserDto user = new UserDto((User) jwtTokenProvider.getAuthentication(accessToken, false).getPrincipal());
            result.put("success", true);
            result.put("data", user);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/personal-info")
    public Map<String, Object> personalInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", "이현기");
            data.put("phone", "010-3332-3266");
            data.put("email", "gusrldlqslek@gmail.com");
            data.put("github", "https://github.com/LeeHyungi1991");
            result.put("success", true);
            result.put("data", data);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @NoLogging
    @GetMapping("/refresh")
    public Map<String, Object> refresh(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
            if (refreshToken == null || !dataBaseService.matchRefreshToken(refreshToken) || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
                throw new CredentialException("잘못된 토큰입니다.");
            }
            Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken, true);
            User user = (User) authentication.getPrincipal();
            String userName = user.getUsername();
            String name = user.getName();
            String accessToken = jwtTokenProvider.createAccessToken(userName, name, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
            dataBaseService.saveAccessToken(userName, accessToken)
                    .orElseThrow(() -> new CredentialException("잘못된 토큰입니다."));
            result.put("success", true);
            result.put("token", accessToken);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("token", null);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @NoLogging
    @GetMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
            String userName = jwtTokenProvider.getAuthentication(refreshToken, true).getName();
            dataBaseService.removeTokens(userName)
                    .map(UserDto::new)
                    .orElseThrow(() -> new CredentialException("로그아웃에 실패하였습니다."));
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .maxAge(0)
                    .httpOnly(true)
                    .path("/")
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
            result.put("success", true);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @NoLogging
    @PostMapping("/social/login")
    public Map<String, Object> googleLogin(@RequestBody Map<String, Object> body, HttpServletResponse response) {
        Map<String, Object> user = new HashMap<>();
        try {
            if (body.containsKey("social")) {
                switch (body.get("social").toString()) {
                    case "google":
                        user = googleService.getGoogleUserInfo(body.get("clientId").toString(), body.get("credential").toString());
                        user.put("social", true);
                        break;
                    case "kakao":
                        user.put("username", "kakaoUser");
                        user.put("social", true);
                        break;
                    default:
                        user.put("social", false);
                        break;
                }
            }
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("token", null);
            result.put("error", e.getMessage());
            return result;
        }
        return login(user, response);
    }

    @NoLogging
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> user, HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = (String) user.get("username");
            String password = (String) user.get("password");
            UserDto userDetails;
            if (user.containsKey("social") && user.get("social").equals(true)) {
                String name = (String) user.get("name");
                dataBaseService.saveUserAtSocialLogin(username, name)
                        .map(UserDto::new)
                        .orElseThrow(() -> new CredentialException("소셜 유저 저장 중 오류가 발생하였습니다."));
            }
            userDetails = dataBaseService.loginUser(username)
                    .map(UserDto::new)
                    .orElseThrow(() -> new CredentialException("잘못된 사용자입니다."));
            if (!user.containsKey("social") || !user.get("social").equals(true)) {
                if (!passwordEncoder.matches(password, userDetails.getPassword())) {
                    throw new CredentialException("잘못된 비밀번호입니다.");
                }
            }
            String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUsername(), userDetails.getName(), userDetails.getAuthorities());
            String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUsername(), userDetails.getName(), userDetails.getAuthorities());
            dataBaseService.saveTokens(userDetails.getUsername(), accessToken, refreshToken)
                    .orElseThrow(() -> new CredentialException("토큰 저장 중 오류가 발생하였습니다."));
            ResponseCookie responseCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .maxAge(refreshTokenExpireTime)
                    .path("/")
                    .secure(true)
                    .httpOnly(true)
                    .build();
            response.setHeader("Set-Cookie", responseCookie.toString());
            result.put("user", userDetails);
            result.put("success", true);
            result.put("token", accessToken);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("token", null);
            result.put("error", e.getMessage());
            log.error(e.getMessage());
        }
        return result;
    }
}
