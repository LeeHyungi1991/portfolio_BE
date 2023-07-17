package com.portfolio.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleService {
    private final JsonFactory jsonFactory;
    private final HttpTransport transport;
    private final WebClient webClient;
    @Value("${spring.cloud.gcp.api.key}")
    private String API_KEY;

    public GoogleService(JsonFactory jsonFactory, HttpTransport transport, WebClient webClient) {
        this.jsonFactory = jsonFactory;
        this.transport = transport;
        this.webClient = webClient;
    }

    public Map<String, Object> getAddressByGoogleMap(Double longitude, Double latitude) throws IOException {
        String lng = String.valueOf(longitude);
        String lat = String.valueOf(latitude);
        String url = String.format("/maps/api/geocode/json?latlng=%s,%s&location_type=ROOFTOP&result_type=street_address&key=%s", lng, lat, API_KEY);
        return jsonFactory.createJsonParser(webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block()).parse(Map.class);
    }

    public Map<String, Object> getGoogleUserInfo(String clientId, String credential) throws GeneralSecurityException, IOException {
        Map<String, Object> userInfo = new HashMap<>();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
        GoogleIdToken idToken = verifier.verify(credential);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.getEmail();
            userInfo.put("username", email);
            boolean emailVerified = payload.getEmailVerified();
            if (!emailVerified) {
                throw new GeneralSecurityException("이메일 인증이 되지 않았습니다.");
            }
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String locale = (String) payload.get("locale");
            String familyName = (String) payload.get("family_name");
            String givenName = (String) payload.get("given_name");
//            System.out.println(name + " " + pictureUrl + " " + locale + " " + familyName + " " + givenName);
            userInfo.put("name", name);
        } else {
            System.out.println("Invalid ID token.");
        }
        return userInfo;
    }
}
