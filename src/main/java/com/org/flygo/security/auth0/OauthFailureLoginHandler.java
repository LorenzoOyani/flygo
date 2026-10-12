package com.org.flygo.security.auth0;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthFailureLoginHandler extends SimpleUrlAuthenticationFailureHandler {
    private static final Logger logger = LoggerFactory.getLogger(OauthFailureLoginHandler.class);

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public void onAuthenticationFailure(
          @NonNull HttpServletRequest request,
          @NonNull   HttpServletResponse response,
          @NonNull   org.springframework.security.core.AuthenticationException exception
    ) throws IOException, ServletException {
        logger.error("Google OAuth2 login failed", exception);
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=oauth2_failed");
    }
}
