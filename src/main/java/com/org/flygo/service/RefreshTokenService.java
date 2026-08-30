package com.org.flygo.service;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.LoginResponse;

public interface RefreshTokenService {
    String createRefreshToken(UserEntity user);

    UserEntity validateAndGetUser(String rawToken);

    void revoke(String rawToken);

    void revokeAll(User user);

    LoginResponse refresh(String rawRefreshToken);
}
