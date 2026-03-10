package com.example.rejunkfrontend.client;

import com.example.rejunkfrontend.dto.*;
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

    public List<ItemDto> getActiveListings() {
        return restClient.get()
                .uri("/listings")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ItemDto getListing(UUID id) {
        return restClient.get()
                .uri("/listings/{id}", id)
                .retrieve()
                .body(ItemDto.class);
    }

    public List<CollectionRequestDto> getAllCollectionRequests() {
        return restClient.get()
                .uri("/collection-requests")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<CollectionRequestDto> getCollectionRequestsByCustomer(UUID customerId) {
        return restClient.get()
                .uri("/collection-requests/customer/{customerId}", customerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public CollectionRequestDto getCollectionRequest(UUID id) {
        return restClient.get()
                .uri("/collection-requests/{id}", id)
                .retrieve()
                .body(CollectionRequestDto.class);
    }

    public CollectionRequestDto createCollectionRequest(CollectionRequestForm form) {
        return restClient.post()
                .uri("/collection-requests")
                .body(form)
                .retrieve()
                .body(CollectionRequestDto.class);
    }

    public List<OrderDto> getOrdersByBuyer(UUID buyerId) {
        return restClient.get()
                .uri("/orders/buyer/{buyerId}", buyerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<NotificationDto> getNotificationsByUser(UUID userId) {
        return restClient.get()
                .uri("/notifications/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
