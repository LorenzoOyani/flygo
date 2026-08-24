package com.org.flygo.security;

import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.UserRoles;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${access-token-expiry-ms}")
    private long accessExpirationTime;



    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserEntity user) {
        return buildToken(user.getEmail(), user.getRole(), accessExpirationTime);
    }


    private String buildToken(String subject, UserRoles roles,  long accessExpirationTime) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .claim("user", roles.name())
                .expiration(Date.from(now.plusMillis(accessExpirationTime)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }
}

