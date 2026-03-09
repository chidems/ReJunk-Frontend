package com.example.rejunkfrontend.client;

import com.example.rejunkfrontend.dto.RegisterRequest;
import com.example.rejunkfrontend.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Service
public class BackendClient {

    private final RestClient restClient;

    public BackendClient(@Value("${backend.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // --- User endpoints ---

    public UserDto registerUser(RegisterRequest request) {
        return restClient.post()
                .uri("/users/register")
                .body(request)
                .retrieve()
                .body(UserDto.class);
    }

    public UserDto getUserById(UUID id) {
        return restClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .body(UserDto.class);
    }

    public List<UserDto> getAllUsers() {
        return restClient.get()
                .uri("/users")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<UserDto> getCustomers() {
        return restClient.get()
                .uri("/users/customers")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public UserDto suspendUser(UUID id) {
        return restClient.patch()
                .uri("/users/{id}/suspend", id)
                .retrieve()
                .body(UserDto.class);
    }

    public UserDto activateUser(UUID id) {
        return restClient.patch()
                .uri("/users/{id}/activate", id)
                .retrieve()
                .body(UserDto.class);
    }

    public void deleteUser(UUID id) {
        restClient.delete()
                .uri("/users/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
