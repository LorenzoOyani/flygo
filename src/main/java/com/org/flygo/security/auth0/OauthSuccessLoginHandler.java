package com.org.flygo.security.auth0;

import com.org.flygo.domain.UserEntity;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.security.authentication.JwtUtil;
import com.org.flygo.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class OauthSuccessLoginHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OauthSuccessLoginHandler.class);

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication
    ) throws IOException {

        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            fail(request, response, "OAuth2 login succeeded but the principal was not an OAuth2User");
            return;
        }

        String rawEmail = oAuth2User.getAttribute("email");
        String email = rawEmail == null ? null : rawEmail.trim().toLowerCase(Locale.ROOT);

        Optional<UserEntity> found = email == null
                ? Optional.empty()
                : userRepository.findByEmail(email);

        if (found.isEmpty()) {
            fail(request, response, "OAuth2 login succeeded but no matching user exists");
            return;
        }
        UserEntity user = found.get();

        String accessToken = jwtUtil.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(30))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void fail(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        logger.error(message);
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=oauth2_failed");
    }
}