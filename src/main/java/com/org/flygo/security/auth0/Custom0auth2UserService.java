package com.org.flygo.security.auth0;

import com.org.flygo.domain.AuthProvider;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.OnBoardingState;
import com.org.flygo.dto.UserRoles;
import com.org.flygo.persistence.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class Custom0auth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @NonNull
    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not provided by Google");
        }

         userRepository.findByEmail(email)
                .map(this::linkGoogleIfNeeded)
                .orElseGet(() -> createGoogleUser(email, fullName));

        return oAuth2User;
    }

    private UserEntity linkGoogleIfNeeded(UserEntity existing) {
        if (existing.getAuthProvider() == AuthProvider.LOCAL) {
            existing.setAuthProvider(AuthProvider.GOOGLE);
            userRepository.save(existing);
        }
        return existing;
    }

    private UserEntity createGoogleUser(String email, String fullName) {
        UserEntity newUser = UserEntity.builder()
                .email(email)
                .fullName(fullName != null ? fullName : email)
                .password(null) // no password for OAuth users
                .role(UserRoles.CUSTOMER)
                .status(OnBoardingState.DOCUMENTS_REQUIRED)
                .authProvider(AuthProvider.GOOGLE)
                .build();
        return userRepository.save(newUser);
    }
}
