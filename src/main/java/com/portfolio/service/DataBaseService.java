package com.portfolio.service;

import com.portfolio.models.dao.LocationDao;
import com.portfolio.models.dao.UserDao;
import com.portfolio.models.dto.LocationDto;
import com.portfolio.models.entity.Location;
import com.portfolio.models.entity.User;
import com.portfolio.util.SftpUtil;
import com.portfolio.util.dijkstra.DijkstraUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class DataBaseService {
    @Value("${spring.sftp.root}")
    private String root;

    private final LocationDao locationDao;
    private final UserDao userDao;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SftpUtil sftpUtil;
    private final HashSet<String> pictureExtensions;

    public DataBaseService(LocationDao locationDao, UserDao userDao, BCryptPasswordEncoder passwordEncoder, SftpUtil sftpUtil, HashSet<String> pictureExtensions) {
        this.locationDao = locationDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.sftpUtil = sftpUtil;
        this.pictureExtensions = pictureExtensions;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<User> saveUserAtSocialLogin(String email, String name) {
        User user = userDao.findByEmail(email).orElseGet(User::new);
        if (user.getSeq() != null) {
            return Optional.of(user);
        }
        user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(new Random().nextInt(1000000) + ""));
        user.setRoles("ROLE_USER");
        user.setCreateAt(new java.sql.Timestamp(System.currentTimeMillis()));
        userDao.save(user);
        return Optional.of(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<User> loginUser(String username) {
        User user = userDao.findByEmail(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        user.setLastLoginAt(new java.sql.Timestamp(System.currentTimeMillis()));
        userDao.save(user);
        return Optional.of(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<User> removeTokens(String userName) {
        User user = userDao.findByEmail(userName).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        user.setAccessToken(null);
        user.setRefreshToken(null);
        userDao.save(user);
        return Optional.of(user);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<User> saveAccessToken(String userName, String accessToken) {
        User user = userDao.findByEmail(userName).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        user.setAccessToken(accessToken);
        userDao.save(user);
        return Optional.of(user);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean matchRefreshToken(String refreshToken) {
        return userDao.findByRefreshToken(refreshToken);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean matchAccessToken(String accessToken) {
        return userDao.findByAccessToken(accessToken);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean existByUserName(String username) {
        return userDao.findByEmail(username).isPresent();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Optional<User> findByUserName(String username) {
        return userDao.findByEmail(username);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<User> saveTokens(String username, String accessToken, String refreshToken) {
        User user = userDao.findByEmail(username).orElse(null);
        if (user == null) {
            return Optional.empty();
        }
        user.setAccessToken(accessToken);
        user.setRefreshToken(refreshToken);
        userDao.save(user);
        return Optional.of(user);
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Location> getLocationsNewestOnTheTop(Double x, Double y, String keyword, Pageable pageRequest) {
        return locationDao.findAllOrderByXYDesc(x, y, keyword, pageRequest);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Optional<Location> saveLocation(LocationDto locationDto, User user, MultipartFile file) throws Exception {
        if (locationDto == null) {
            throw new IllegalArgumentException("locationDto is null");
        }
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }
        if (locationDto.getName() == null || locationDto.getName().isEmpty()) {
            throw new IllegalArgumentException("위치명을 입력하지 않았습니다.");
        }
        if (locationDto.getYAxis() == null || locationDto.getXAxis() == null) {
            throw new IllegalArgumentException("위치정보가 없습니다.");
        }
        if (locationDto.getAddress() == null || locationDto.getAddress().isEmpty()) {
            throw new IllegalArgumentException("주소를 입력하지 않았습니다.");
        }
        if (locationDto.getDetails().length() > 1000) {
            throw new IllegalArgumentException("상세정보는 1000자 이내로 입력해주세요.");
        }
        Location location;
        if (locationDto.getSeq() == null) {
            location= locationDao.findByXY(locationDto.getXAxis(), locationDto.getYAxis()).orElseGet(Location::new);
        } else {
            location = locationDao.findById(locationDto.getSeq()).orElse(null);
            if (location == null) {
                throw new IllegalArgumentException("위치정보가 없습니다.");
            }
        }
//        if (location.getUser() != null && !Objects.equals(location.getUser().getSeq(), user.getSeq())) {
//            throw new Exception("이미 다른 사용자에 의해 등록된 위치입니다.");
//        }
        location.setName(locationDto.getName());
        location.setDetails(locationDto.getDetails());
        location.setAddress(locationDto.getAddress());
        location.setXAxis(locationDto.getXAxis());
        location.setYAxis(locationDto.getYAxis());
        if (file != null) {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
            if (!pictureExtensions.contains(extension)) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
            }
            location.setImagePath("/mapImages/" + file.getOriginalFilename());
        }
        if (location.getCreateAt() == null) {
            location.setCreateAt(new java.sql.Timestamp(System.currentTimeMillis()));
        }
        location.setUser(user);
        locationDao.save(location);
        if (file != null) {
            try {
                sftpUtil.uploadFile(root + location.getImagePath(), file.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            location.setImagePath(null);
            if (sftpUtil.existFile(root + location.getImagePath())) {
                sftpUtil.deleteFile(root + location.getImagePath());
            }
        }
        return Optional.of(location);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteLocation(LocationDto locationDto, User user) {
        if (locationDto == null) {
            throw new IllegalArgumentException("locationDto is null");
        }
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }
        Location location = locationDao.findById(locationDto.getSeq()).orElse(null);
        if (location == null) {
            throw new IllegalArgumentException("location is null");
        }
        if (!location.getUser().getSeq().equals(user.getSeq())) {
            throw new IllegalArgumentException("다른 사용자가 등록한 위치입니다.");
        }
        locationDao.delete(location);
        try {
            if (location.getImagePath() != null && !location.getImagePath().isEmpty()) {
                sftpUtil.deleteFile(root + location.getImagePath());
            }
        } catch (Exception ignored) {
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Location> dijkstraPath(double lat, double lon, double x2, double y2) {
        Location start = locationDao.findByClosestXY(lat, lon).orElse(null);
        Location end = locationDao.findByClosestXY(x2, y2).orElse(null);
        if (start == null || end == null) {
            throw new IllegalArgumentException("출발지와 목적지가 입력되지 않았습니다.");
        }
        return DijkstraUtil.getShortestPath(start, end);
    }
}
