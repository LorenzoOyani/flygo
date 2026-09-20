package com.org.flygo.security.auth0;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;


@Component
public class OAuth2AwareLogoutHandler implements LogoutHandler {
    @Override
    public void logout(

            @Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Authentication authentication) {

        assert request != null;
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

    }
}
