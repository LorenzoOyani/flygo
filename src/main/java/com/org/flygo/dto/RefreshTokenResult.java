package com.org.flygo.dto;

import com.org.flygo.domain.UserEntity;

public record RefreshTokenResult(
        String newRefreshToken,
        UserEntity user
) {
}
