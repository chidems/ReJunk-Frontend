package com.example.rejunkfrontend.dto;

import java.math.BigDecimal;

public record EvaluateItemRequest(String itemCondition, BigDecimal evaluatedPrice) {}