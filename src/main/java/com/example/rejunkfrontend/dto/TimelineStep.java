package com.example.rejunkfrontend.dto;

// One step in the collection-detail status timeline.
public record TimelineStep(
        String label,
        String date,
        boolean pending
) {}
