package com.example.demo.kakao_authentication.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class KakaoAuthenticationRepositoryImpl implements KakaoAuthenticationRepository {
    private final String loginUrl;
    private final String clientId;
    private final String redirectUri;
    private final String tokenRequestUri;
    private final String userInfoRequestUri;

    private final RestTemplate restTemplate;

    public KakaoAuthenticationRepositoryImpl(
            @Value("${kakao.login-url}") String loginUrl,
            @Value("${kakao.client-id}") String clientId,
            @Value("${kakao.redirect-uri}") String redirectUri,
            @Value("${kakao.token-request-uri}") String tokenRequestUri,
            @Value("${kakao.user-info-request-uri}") String userInfoRequestUri,
            RestTemplate restTemplate
    ) {
        this.loginUrl = loginUrl;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.tokenRequestUri = tokenRequestUri;
        this.userInfoRequestUri = userInfoRequestUri;

        this.restTemplate = restTemplate;
    }

    public String getLoginLink() {
        System.out.println("getLoginLink() for Login");
        return String.format("%s/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                loginUrl, clientId, redirectUri);
    }

    public Map<String, Object> getAccessToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("redirect_uri", redirectUri);
        formData.add("code", code);
        formData.add("client_secret", "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenRequestUri, HttpMethod.POST, entity, Map.class);

        return response.getBody();
    }

    @Override
    public Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoRequestUri, HttpMethod.GET, entity, Map.class);

        log.info("User Info: {}", response.getBody());

        return response.getBody();
    }
}
