package com.org.flygo.service;

import com.org.flygo.dto.*;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;


public interface AuthService {

    AuthResponse register(SignUpRequest request);

    LoginResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(@NotBlank String request);

    void logout(@NotBlank String rawRefreshToken);

    void logoutAllDevices(UUID uuid);


}
