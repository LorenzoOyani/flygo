package com.org.flygo.service.impl;

import com.org.flygo.agents.User;
import com.org.flygo.domain.UserEntity;
import com.org.flygo.dto.*;
import com.org.flygo.exception.InvalidCredentialsException;
import com.org.flygo.exception.UserAlreadyExists;
import com.org.flygo.exception.UserNotFoundException;
import com.org.flygo.mapper.UserMapper;
import com.org.flygo.persistence.UserRepository;
import com.org.flygo.security.JwtUtil;
import com.org.flygo.service.AuthService;
import com.org.flygo.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(SignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExists("user already exits");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRoles.CUSTOMER)
                .build();

        UserEntity user1 = userMapper.toUserEntity(user);

        logger.info("Registering user {} successful", user1);
        userRepository.save(user1);

        return generateToken(user1);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {

        String email = loginRequest.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    loginRequest.password()
                            )
                    );
//
//            SecurityContextHolder
//                    .getContext()
//                    .setAuthentication(authentication);

            logger.info(
                    "User authenticated successfully: {}",
                    email
            );

            UserEntity user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new InvalidCredentialsException(
                                    "Invalid email or password"
                            )
                    );
            User user1 = userMapper.toUserEntity(user);

//            if (!user.isEnabled()) {
//                throw new AccountDisabledException(
//                        "Account is disabled"
//                );
//            }

            String accessToken =
                    jwtUtil.generateToken(user);

            String refreshToken =
                    refreshTokenService.createRefreshToken(user1);

            return new LoginResponse(
                    user.getId(),
                    accessToken,
                    refreshToken,
                    user.getStatus()
            );

        } catch (BadCredentialsException e) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }
    }

    ///  still to add
    /// rotate Refresh tokens implementations

    private AuthResponse generateToken(UserEntity user) {
        User user1 = userMapper.toUserEntity(user);
        String token = jwtUtil.generateToken(user);

        String refreshToken = refreshTokenService.createRefreshToken(user1);

        /// todo-
        /// redis to cache refresh tokens (application write along cache);

        return new AuthResponse(
                user.getId(),
                token,
                refreshToken,
                user.getStatus()

        );
    }
}
