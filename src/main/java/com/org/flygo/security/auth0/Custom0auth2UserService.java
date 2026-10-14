package com.org.flygo.security.auth0;

import com.org.flygo.domain.AuthProvider;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.persistence.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class Custom0auth2UserService extends OidcUserService {

    private static final Logger logger = LoggerFactory.getLogger(Custom0auth2UserService.class);

    private final UserRepository userRepository;

    @Override
    @NonNull
    public OidcUser loadUser(@NonNull OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        if (oidcUser.getEmail() == null || !Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_not_verified"), "Google email missing or not verified");
        }
        String email = oidcUser.getEmail().trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    if (existing.getAuthProvider() == AuthProvider.LOCAL) {
                        existing.setAuthProvider(AuthProvider.GOOGLE);
                        existing.setPassword(null); // discard any password set before ownership was proven
                        userRepository.save(existing);
                        logger.info("Linked an existing local account to Google");
                    }
                },
                () -> {
                    userRepository.save(UserEntity.builder()
                            .email(email)
                            .fullName(oidcUser.getFullName() != null ? oidcUser.getFullName() : email)
                            .password(null)
                            .status(OnBoardingState.DOCUMENTS_REQUIRED)
                            .authProvider(AuthProvider.GOOGLE)
                            .build());
                    logger.info("Created a new Google user");
                });

        return oidcUser;
    }
}