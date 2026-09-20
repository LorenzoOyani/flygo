package com.org.flygo.security.auth0;

import com.org.flygo.domain.UserEntity;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.security.authentication.JwtUtil;
import com.org.flygo.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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


@Component
@RequiredArgsConstructor
public class OauthSuccessLoginHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
           @NonNull HttpServletRequest request,
           @NonNull   HttpServletResponse response,
           @NonNull    Authentication authentication
    )throws IOException {

            String email = null;

            final OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            if(oAuth2User == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }else {

                email = oAuth2User.getAttribute("email");
            }
            UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("email not found"));

            String accessToken = jwtUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(Duration.ofMinutes(10))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);


    }



}
