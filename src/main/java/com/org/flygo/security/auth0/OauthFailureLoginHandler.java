package com.org.flygo.security.auth0;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthFailureLoginHandler extends SimpleUrlAuthenticationFailureHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    public void onAuthenticationFailure(
          @NonNull HttpServletRequest request,
          @NonNull   HttpServletResponse response,
          @NonNull   org.springframework.security.core.AuthenticationException exception
    ) throws IOException, ServletException {
        getRedirectStrategy().sendRedirect(request, response, redirectUri + "?error=oauth2_failed");
    }
}
