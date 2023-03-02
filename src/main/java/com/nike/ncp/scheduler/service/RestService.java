package com.nike.ncp.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RestService {

    @Value("${rest.service.auto.retires:1}")
    private int autoRetries;

    private final RestTemplate restTemplate;

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public <T, R> T exchange(String url, HttpMethod method, HttpHeaders headers, R body, Class<T> responseType) {
        @SuppressWarnings("all")
        HttpEntity request = new HttpEntity(body, headers);
        for (int i = 0; i <= autoRetries; i++) {
            try {
                ResponseEntity<T> response = restTemplate.exchange(url, method, request, responseType);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Failed to call API, url=" + url
                            + ", method=" + method + ", status=" + response.getStatusCode());
                }
                return response.getBody();
            } catch (RestClientException e) {
                log.warn("event=api_call_timout, url={}, message={}", url, e.getMessage());
            }
        }
        throw new RuntimeException("Failed to call API, url=" + url + ", method=" + method);
    }

    public <T> T get(String url, Class<T> responseType) {
        return exchange(url, HttpMethod.GET, createHeaders(), null, responseType);
    }

    public <T> T getBearerToken(String url, String bearerToken, Class<T> responseType) {
        HttpHeaders headers = createHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, bearerToken);
        return exchange(url, HttpMethod.GET, headers, null, responseType);
    }

    public <T, R> T post(String url, R body, Class<T> responseType) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return exchange(url, HttpMethod.POST, headers, body, responseType);
    }

    public <T, R> T postBearerToken(String url, R body, String bearerToken, Class<T> responseType) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, bearerToken);
        return exchange(url, HttpMethod.POST, headers, body, responseType);
    }

    public <T, R> T post(String url, R body, HttpHeaders headers, Class<T> responseType) {
        return exchange(url, HttpMethod.POST, headers, body, responseType);
    }
}
