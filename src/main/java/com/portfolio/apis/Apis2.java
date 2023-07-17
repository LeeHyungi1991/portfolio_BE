package com.portfolio.apis;

import com.portfolio.configs.JwtConfig;
import com.portfolio.handlers.WebsocketHandler;
import com.portfolio.models.dto.LocationDto;
import com.portfolio.models.dto.MailDto;
import com.portfolio.models.dto.RoomDto;
import com.portfolio.models.entity.User;
import com.portfolio.service.DataBaseService;
import com.portfolio.service.GoogleService;
import com.portfolio.service.MailService;
import com.portfolio.util.JsoupScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class Apis2 {
    private final DataBaseService dataBaseService;
    private final JwtConfig jwtTokenProvider;
    private final GoogleService googleService;
    private final MailService mailService;

    @GetMapping("/get/locations")
    public Map<String, Object> getLocations(@RequestParam("x") Double x, @RequestParam("y") Double y, @RequestParam(name = "keyword", required = false) String keyword, @RequestParam(name = "page", required = false, defaultValue = "0") Integer page, @RequestParam(name = "size", required = false, defaultValue = "5") Integer size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        Map<String, Object> result = new HashMap<>();
        try {
            Page<LocationDto> locationPage = dataBaseService.getLocationsNewestOnTheTop(x, y, keyword, pageable).stream().map(LocationDto::new).collect(Collectors.collectingAndThen(Collectors.toList(), locationDtos -> new org.springframework.data.domain.PageImpl<>(locationDtos, pageable, locationDtos.size())));
            result.put("success", true);
            result.put("data", locationPage);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @GetMapping("/get/address")
    public Map<String, Object> getAddress(@RequestParam("x") Double x, @RequestParam("y") Double y) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> address = googleService.getAddressByGoogleMap(x, y);
            result.put("success", true);
            result.put("data", address);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }
    @GetMapping("/get/omok/rooms")
    public Map<String, Object> getOmokRooms() {
        Map<String, Object> result = new HashMap<>();
        List<RoomDto> roomDtos = WebsocketHandler.getOmokRooms();
        try {
            result.put("success", true);
            result.put("data", roomDtos);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }
    @PostMapping(path = "/post/location/save", consumes = {"multipart/form-data"})
    public Map<String, Object> saveLocation(@ModelAttribute("locationDto") LocationDto locationDto, HttpServletRequest request, @RequestParam(name = "file", required = false) MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        User user = ((User) jwtTokenProvider.getAuthentication(refreshToken, true).getPrincipal());
        try {
            locationDto = dataBaseService.saveLocation(locationDto, user, file).map(LocationDto::new).orElse(null);
            result.put("success", true);
            result.put("data", locationDto);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @PostMapping("/post/location/delete")
    public Map<String, Object> deleteLocation(@RequestBody LocationDto locationDto, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        User user = ((User) jwtTokenProvider.getAuthentication(refreshToken, true).getPrincipal());
        try {
            dataBaseService.deleteLocation(locationDto, user);
            result.put("success", true);
            result.put("data", true);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }
    @PostMapping("/post/scrap")
    public Map<String, Object> getScrap(@RequestBody Map<String, Object> params) {
        String url = (String) params.get("url");
        String selector = (String) params.get("selector");
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("success", true);
            result.put("data", JsoupScraper.scrap(url, selector));
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }
    @PostMapping(path="/post/mail/send-mail", consumes = {"multipart/form-data"})
    public Map<String, Object> sendMail(@ModelAttribute("mailDto") MailDto mailDto, @RequestParam(name = "files[]", required = false) MultipartFile[] files, HttpServletRequest request) {
        if (files == null) {
            files = new MultipartFile[0];
        }
        Map<String, Object> result = new HashMap<>();
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
            User user = ((User) jwtTokenProvider.getAuthentication(refreshToken, true).getPrincipal());
            mailService.sendMail(new String[]{user.getEmail()}, mailDto.getSubject(), mailDto.getMessage(), Arrays.stream(files).collect(Collectors.toList()));
            result.put("success", true);
            result.put("data", true);
            result.put("error", null);
        } catch (Exception e) {
            result.put("success", false);
            result.put("data", null);
            result.put("error", e.getMessage());
        }
        return result;
    }
}
