package com.ordermanagement.order.client;

import com.ordermanagement.order.exceptions.OrderException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
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
        }
        catch (HttpStatusCodeException ex) {

            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

            throw new OrderException(
                    buildCleanMessage(url, status),
                    "EXTERNAL_SERVICE_ERROR",
                    status,
                    "DOWNSTREAM_ERROR"
            );
        }
        catch (RestClientException ex) {

            throw new OrderException(
                    "Service unavailable: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }


    public <T, R> T post(String url, R request, Class<T> responseType) {
        try {
            return restTemplate.postForObject(url, request, responseType);
        }
        catch (HttpStatusCodeException ex) {

            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

            throw new OrderException(
                    buildCleanMessage(url, status),
                    "EXTERNAL_SERVICE_ERROR",
                    status,
                    "DOWNSTREAM_ERROR"
            );
        }
        catch (RestClientException ex) {

            throw new OrderException(
                    "Service unavailable: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }


    public void postVoid(String url, Object request) {
        try {
            restTemplate.postForObject(url, request, Void.class);
        }
        catch (HttpStatusCodeException ex) {

            HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

            throw new OrderException(
                    buildCleanMessage(url, status),
                    "EXTERNAL_SERVICE_ERROR",
                    status,
                    "DOWNSTREAM_ERROR"
            );
        }
        catch (RestClientException ex) {

            throw new OrderException(
                    "Service unavailable: " + url,
                    "EXTERNAL_SERVICE_ERROR",
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_DOWN"
            );
        }
    }


    private String buildCleanMessage(String url, HttpStatus status) {

        switch (status) {
            case NOT_FOUND:
                return "Resource not found at: " + url;

            case BAD_REQUEST:
                return "Bad request sent to: " + url;

            case UNAUTHORIZED:
                return "Unauthorized request to: " + url;

            case FORBIDDEN:
                return "Access forbidden to: " + url;

            case INTERNAL_SERVER_ERROR:
                return "Downstream service error at: " + url;

            default:
                return "Service call failed at: " + url;
        }
    }
}