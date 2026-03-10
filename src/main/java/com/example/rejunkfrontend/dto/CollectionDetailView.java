package com.example.rejunkfrontend.dto;

import java.util.List;
import java.util.UUID;

public record CollectionDetailView(
        UUID id,
        String date,
        String status,
        String address,
        String pickupDate,
        String pickupTime,
        List<TimelineStep> timeline
) {}
