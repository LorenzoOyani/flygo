package com.org.flygo.service;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.LoginResponse;
import com.org.flygo.dto.RefreshTokenResult;

public interface RefreshTokenService {
    String createRefreshToken(UserEntity user);

    UserEntity validateAndGetUser(String rawToken);

    void revoke(String rawToken);

    void revokeAll(UserEntity user);

    LoginResponse refresh(String rawRefreshToken);

    RefreshTokenResult rotateRefreshToken(String rawToken);
}
