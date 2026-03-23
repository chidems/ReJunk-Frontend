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

    // ── Auth ─────────────────────────────────────────────────────────────────

    public AuthResponse register(RegisterRequest request) {
        return restClient.post()
                .uri("/auth/register")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }

    public AuthResponse login(LoginRequest request) {
        return restClient.post()
                .uri("/auth/login")
                .body(request)
                .retrieve()
                .body(AuthResponse.class);
    }

    // ── Users ─────────────────────────────────────────────────────────────────

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

    // ── Collection Requests ───────────────────────────────────────────────────

    public List<CollectionRequestDto> getAllCollectionRequests() {
        return restClient.get()
                .uri("/collection-requests")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<CollectionRequestDto> getCollectionRequestsByUser(UUID userId) {
        return restClient.get()
                .uri("/collection-requests/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public CollectionRequestDto getCollectionRequest(UUID id) {
        return restClient.get()
                .uri("/collection-requests/{id}", id)
                .retrieve()
                .body(CollectionRequestDto.class);
    }

    public CollectionRequestDto createCollectionRequest(CreateCollectionRequestRequest request) {
        return restClient.post()
                .uri("/collection-requests")
                .body(request)
                .retrieve()
                .body(CollectionRequestDto.class);
    }

    public CollectionRequestDto updateCollectionRequestStatus(UUID id, String status) {
        return restClient.patch()
                .uri("/collection-requests/{id}/status?status={status}", id, status)
                .retrieve()
                .body(CollectionRequestDto.class);
    }

    // ── Items ─────────────────────────────────────────────────────────────────

    public ItemDto createItem(CreateItemRequest request) {
        return restClient.post()
                .uri("/items")
                .body(request)
                .retrieve()
                .body(ItemDto.class);
    }

    public ItemDto getItem(UUID id) {
        return restClient.get()
                .uri("/items/{id}", id)
                .retrieve()
                .body(ItemDto.class);
    }

    public List<ItemDto> getItemsByCollectionRequest(UUID collectionRequestId) {
        return restClient.get()
                .uri("/items/collection-request/{collectionRequestId}", collectionRequestId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ItemDto evaluateItem(UUID id, EvaluateItemRequest request) {
        return restClient.patch()
                .uri("/items/{id}/evaluate", id)
                .body(request)
                .retrieve()
                .body(ItemDto.class);
    }

    // ── Listings ──────────────────────────────────────────────────────────────

    public ItemDto createListing(CreateListingRequest request) {
        return restClient.post()
                .uri("/listings")
                .body(request)
                .retrieve()
                .body(ItemDto.class);
    }

    public List<ItemDto> getActiveListings() {
        return restClient.get()
                .uri("/listings")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<ItemDto> getAllListings() {
        return restClient.get()
                .uri("/listings/all")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public ItemDto getListing(UUID id) {
        return restClient.get()
                .uri("/listings/{id}", id)
                .retrieve()
                .body(ItemDto.class);
    }

    public ItemDto updateListingStatus(UUID id, UpdateListingStatusRequest request) {
        return restClient.patch()
                .uri("/listings/{id}/status", id)
                .body(request)
                .retrieve()
                .body(ItemDto.class);
    }

    // ── Orders ────────────────────────────────────────────────────────────────

    public OrderDto createOrder(CreateOrderRequest request) {
        return restClient.post()
                .uri("/orders")
                .body(request)
                .retrieve()
                .body(OrderDto.class);
    }

    public OrderDto getOrder(UUID id) {
        return restClient.get()
                .uri("/orders/{id}", id)
                .retrieve()
                .body(OrderDto.class);
    }

    public List<OrderDto> getOrdersByBuyer(UUID buyerId) {
        return restClient.get()
                .uri("/orders/buyer/{buyerId}", buyerId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<OrderDto> getAllOrders() {
        return restClient.get()
                .uri("/orders")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public OrderDto updateOrderStatus(UUID id, UpdateOrderStatusRequest request) {
        return restClient.patch()
                .uri("/orders/{id}/status", id)
                .body(request)
                .retrieve()
                .body(OrderDto.class);
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    public List<NotificationDto> getNotificationsByUser(UUID userId) {
        return restClient.get()
                .uri("/notifications/user/{userId}", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}