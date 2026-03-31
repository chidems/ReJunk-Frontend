package com.example.rejunkfrontend.dto;

import java.util.UUID;

public record CreateItemRequest(
        UUID collectionRequestId,
        String title,
        String description,
        String condition
) {}