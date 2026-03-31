package com.example.rejunkfrontend.dto;

import java.util.UUID;

public record DashboardRequestView(UUID id, String date, String time, String address, int itemCount, String status) {}