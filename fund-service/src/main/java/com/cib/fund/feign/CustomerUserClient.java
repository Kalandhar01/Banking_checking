package com.cib.fund.feign;

import com.cib.fund.dto.ApiResponse;
import com.cib.fund.dto.CustomerUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerUserClient {

    private final RestTemplate restTemplate;
    private static final String USER_URL = "http://customer-service/customer/users";

    public CustomerUserDto getUser(Long userId) {
        try {
            String url = USER_URL + "/" + userId;
            var response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<ApiResponse<CustomerUserDto>>() {}
            );
            ApiResponse<CustomerUserDto> body = response.getBody();
            if (body != null && body.isSuccess()) {
                return body.getData();
            }
            return null;
        } catch (ResourceAccessException e) {
            log.error("Customer service unavailable: {}", e.getMessage());
            throw new RuntimeException("Customer service is unavailable");
        } catch (Exception e) {
            log.error("Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }
}
