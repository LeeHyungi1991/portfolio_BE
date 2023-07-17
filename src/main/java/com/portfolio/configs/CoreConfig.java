package com.portfolio.configs;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;

@Configuration
@EnableJpaRepositories(basePackages = "com.portfolio")
public class CoreConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.create("https://maps.googleapis.com");
    }
    @Bean
    public JsonFactory jsonFactory() {
        return GsonFactory.getDefaultInstance();
    }
    @Bean
    public HttpTransport httpTransport() {
        return new com.google.api.client.http.javanet.NetHttpTransport();
    }
    @Bean
    HashSet<String> pictureExtensions() {
        HashSet<String> pictureExtensions = new HashSet<>();
        pictureExtensions.add("jpg");
        pictureExtensions.add("jpeg");
        pictureExtensions.add("png");
        pictureExtensions.add("gif");
        pictureExtensions.add("jfif");
        return pictureExtensions;
    }
}
