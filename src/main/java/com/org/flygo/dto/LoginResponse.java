package com.org.flygo.dto;

import java.util.UUID;

public record LoginResponse(
        UUID id,
        String accessToken,
        String refreshToken,
        OnBoardingState staus

) {
}
