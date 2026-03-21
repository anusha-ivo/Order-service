package com.ordermanagement.order.client;

import com.ordermanagement.order.exceptions.OrderException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RestClientService {

    private final RestTemplate restTemplate;


    public RestClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T get(String url, Class<T> responseType) {
        try {
            return restTemplate.getForObject(url, responseType);
        } catch (RestClientException ex) {
            throw new OrderException(
                    "GET call failed: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }

    public <T, R> T post(String url, R request, Class<T> responseType) {
        try {
            return restTemplate.postForObject(url, request, responseType);
        } catch (RestClientException ex) {
            throw new OrderException(
                    "POST call failed: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }

    public void postVoid(String url, Object request) {
        try {
            restTemplate.postForObject(url, request, Void.class);
        } catch (RestClientException ex) {
            throw new OrderException(
                    "POST VOID call failed: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }
}