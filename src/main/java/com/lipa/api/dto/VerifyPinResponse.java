package com.lipa.api.dto;

public record VerifyPinResponse(
        boolean success,
        boolean cardBlocked
) {}
