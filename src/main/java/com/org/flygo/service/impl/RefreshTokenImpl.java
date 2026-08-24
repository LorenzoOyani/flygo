package com.org.flygo.service.impl;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.LoginResponse;
import com.org.flygo.exception.InvalidTokenException;
import com.org.flygo.mapper.UserMapper;
import com.org.flygo.persistence.RefreshTokenRepository;
import com.org.flygo.security.JwtUtil;
import com.org.flygo.security.entity.RefreshToken;
import com.org.flygo.service.RefreshTokenService;
import com.org.flygo.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenImpl implements RefreshTokenService {

    private final UserMapper userMapper;
    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int TOKEN_LENGTH = 32;

    private static final long REFRESH_TOKEN_DAYS = 30;


    @Override
    public String createRefreshToken(User users) {

        final String rawToken = generateRawToken();

        String hashToken = tokenHasher.hash(rawToken);
        UserEntity user = userMapper.toUserEntity(users);

        RefreshToken refreshToken = new RefreshToken(
                user,
                hashToken,
                Instant.now().plus(
                        REFRESH_TOKEN_DAYS, ChronoUnit.DAYS
                )
        );
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    /**
    * Get a validated user with a refreshed token safely hashed
    * @Param -  rawToken is the token o be hash
    * @return - a User Entity
    **/

    @Override
    public UserEntity validateAndGetUser(String rawToken) {
        String hashToken = tokenHasher.hash(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenHash(
                        hashToken
                ).orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token is revoked");
        }

        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("Refresh token is expired");
        }

        return refreshToken.getUser();

    }

    @Override
    public void revoke(String rawToken) {
        String tokenHash =
                tokenHasher.hash(rawToken);

        RefreshToken session =
                refreshTokenRepository
                        .findByTokenHash(tokenHash).orElseThrow(() -> new InvalidTokenException("Refresh token not found"));


        session.revoke();

    }

    @Override
    public void revokeAll(User users) {

        UserEntity user = userMapper.toUserEntity(users);
        List<RefreshToken> refreshTokens =
                refreshTokenRepository.findAllByUserAndRevokedAtIsNull(user);

        refreshTokens.forEach(
                RefreshToken::revoke
        );
    }

    @Override
    public LoginResponse refresh(String rawRefreshToken) {

        UserEntity user = this.validateAndGetUser(rawRefreshToken);

        String newAccessToken = this.jwtUtil.generateToken(user);

        return new LoginResponse(
                user.getId(),
                newAccessToken,
                rawRefreshToken,
                user.getStatus()

        );
    }

    private String generateRawToken() {
        byte[] bytes =
                new byte[TOKEN_LENGTH];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
