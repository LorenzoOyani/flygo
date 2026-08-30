package com.org.flygo.service.impl;

import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.LoginResponse;
import com.org.flygo.dto.RefreshTokenResult;
import com.org.flygo.exception.InvalidTokenException;
import com.org.flygo.persistence.RefreshTokenRepository;
import com.org.flygo.security.JwtUtil;
import com.org.flygo.security.entity.RefreshToken;
import com.org.flygo.service.RefreshTokenService;
import com.org.flygo.util.TokenHasher;
import jakarta.transaction.Transactional;
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

    private final TokenHasher tokenHasher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int TOKEN_LENGTH = 32;

    private static final long REFRESH_TOKEN_DAYS = 30;


    @Override
    public String createRefreshToken(UserEntity users) {

        final String rawToken = generateRawToken();

        String hashToken = tokenHasher.hash(rawToken);

        RefreshToken refreshToken = new RefreshToken(
                users,
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
    @Transactional
    public UserEntity validateAndGetUser(String rawToken) {
        String hashToken = tokenHasher.hash(rawToken);

        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenHash(
                        hashToken
                ).orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            revokeAll(refreshToken.getUser());
            throw new InvalidTokenException("Refresh token is revoked");
        }

        if (refreshToken.isExpired()) {
            throw new InvalidTokenException("Refresh token is expired");
        }

        return refreshToken.getUser();

    }

    @Override
    @Transactional
    public void revoke(String rawToken) {
        String tokenHash =
                tokenHasher.hash(rawToken);

        RefreshToken session =
                refreshTokenRepository
                        .findByTokenHash(tokenHash).orElseThrow(() -> new InvalidTokenException("Refresh token not found"));


        session.revoke();

    }

    @Override
    @Transactional
    public void revokeAll(UserEntity users) {

        List<RefreshToken> refreshTokens =
                refreshTokenRepository.findAllByUserAndRevokedAtIsNull(users);

        refreshTokens.forEach(
                RefreshToken::revoke
        );
    }

    @Override
    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {

        UserEntity user = this.validateAndGetUser(rawRefreshToken);

        revoke(rawRefreshToken);

        String newAccessToken = this.jwtUtil.generateToken(user);
        String newRefreshToken = this.createRefreshToken(user);

        return new LoginResponse(
                user.getId(),
                newAccessToken,
                newRefreshToken,
                user.getStatus()

        );
    }

    @Override
    public RefreshTokenResult rotateRefreshToken(String rawToken) {
        String hashedToken = tokenHasher.hash(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hashedToken)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.isRevoked()) {
            // Token reuse detected — someone used an already-rotated token.
            // Revoke the entire session/family as a precaution.
            refreshTokenRepository.revokeAllForUser(existing.getUser().getId());
            throw new InvalidTokenException("Refresh token reuse detected — session revoked");
        }

        if (existing.isExpired()) {
            throw new InvalidTokenException("Refresh token expired");
        }

        // Revoke the old token (single-use enforcement)
        existing.revoke();
        refreshTokenRepository.save(existing);

        // Issue a new one
        UserEntity user = existing.getUser();
        String newRawToken = createRefreshToken(user);

        return new RefreshTokenResult(newRawToken, user);
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
