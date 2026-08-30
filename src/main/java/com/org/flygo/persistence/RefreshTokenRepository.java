package com.org.flygo.persistence;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.security.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(
            String tokenHash
    );

    List<RefreshToken> findAllByUserAndRevokedAtIsNull(
            UserEntity user
    );

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = CURRENT_TIMESTAMP WHERE r.user.id = :userId AND r.revokedAt IS NULL")
    void revokeAllForUser(UUID id);
}
