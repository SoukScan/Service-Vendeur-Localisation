package com.soukscan.vendorms.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductClient {

    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8082/api/products}")
    private String productServiceUrl;

    public Map<String, Object> getProductById(Long productId) {
        try {
            String url = productServiceUrl + "/" + productId;
            log.info("Fetching product from: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Product service unavailable: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> getAllProducts() {
        try {
            String url = productServiceUrl;
            log.info("Fetching all products from: {}", url);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching products: {}", e.getMessage());
            throw new RuntimeException("Product service unavailable: " + e.getMessage());
        }
    }

    public List<Map<String, Object>> searchProducts(String name) {
        try {
            String url = productServiceUrl + "/search?name=" + name;
            log.info("Searching products from: {}", url);

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Error searching products: {}", e.getMessage());
            throw new RuntimeException("Product service unavailable: " + e.getMessage());
        }
    }
}

