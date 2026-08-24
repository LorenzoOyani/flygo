package com.org.flygo.persistence;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(
            UserEntity user
    );
}
