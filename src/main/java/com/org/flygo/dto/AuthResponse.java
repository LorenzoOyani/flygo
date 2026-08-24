package com.org.flygo.dto;

import java.util.UUID;

public record AuthResponse(UUID id, String accessToken, String refreshToken, OnBoardingState status) {
}
