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

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class Custom0auth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @NonNull
    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String rawEmail = (String) attributes.get("email");
        if (rawEmail == null || !Boolean.TRUE.equals(attributes.get("email_verified"))) {
            throw new OAuth2AuthenticationException("Google email missing or not verified");
        }
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);

        userRepository.findByEmail(email).ifPresentOrElse(
                existing -> {
                    if (existing.getAuthProvider() == AuthProvider.LOCAL) {
                        existing.setAuthProvider(AuthProvider.GOOGLE);
                        existing.setPassword(null); // discard any password set before the real owner proved the email
                        userRepository.save(existing);
                    }
                },
                () -> createGoogleUser(email, (String) attributes.get("name"))
        );
//        String email = (String) attributes.get("email");
//        String fullName = (String) attributes.get("name");
//
//        if (email == null) {
//            throw new OAuth2AuthenticationException("Email not provided by Google");
//        }
//
//         userRepository.findByEmail(email)
//                .map(this::linkGoogleIfNeeded)
//                .orElseGet(() -> createGoogleUser(email, fullName));

        return oAuth2User;
    }

//    private UserEntity linkGoogleIfNeeded(UserEntity existing) {
//        if (existing.getAuthProvider() == AuthProvider.LOCAL) {
//            existing.setAuthProvider(AuthProvider.GOOGLE);
//            userRepository.save(existing);
//        }
//        return existing;
//    }

    private UserEntity createGoogleUser(String email, String fullName) {
        UserEntity newUser = UserEntity.builder()
                .email(email)
                .fullName(fullName != null ? fullName : email)
                .password(null)
                .roles(Set.of(UserRoles.CUSTOMER))
                .status(OnBoardingState.DOCUMENTS_REQUIRED)
                .authProvider(AuthProvider.GOOGLE)
                .build();
        return userRepository.save(newUser);
    }
}
