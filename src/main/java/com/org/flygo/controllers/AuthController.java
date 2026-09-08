package com.org.flygo.controllers;



import com.org.flygo.dto.*;
import com.org.flygo.service.AuthService;
import com.org.flygo.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Signup, login, token refresh and logout endpoints")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";
    private static final String AUTH_COOKIE_PATH = "/api/v1/auth";
    private static final Duration REFRESH_TOKEN_MAX_AGE = Duration.ofDays(30);

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @SecurityRequirements
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> authSignup(
            @Valid @RequestBody SignUpRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request);
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Log in with email and password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.login(request);
        setRefreshTokenCookie(response, loginResponse.refreshToken());
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Log out and revoke the current refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(REFRESH_TOKEN_COOKIE) String rawRefreshToken,
            HttpServletResponse response
    ) {
        authService.logout(rawRefreshToken);
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Exchange a refresh token for a new access/refresh token pair")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(REFRESH_TOKEN_COOKIE) String rawRefreshToken,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refreshToken(rawRefreshToken);
        // Rotation issues a NEW refresh token — the cookie must be updated to match,
        // or the old (now-revoked) token stays cached in the browser and breaks the next refresh.
        setRefreshTokenCookie(response, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawRefreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .path(AUTH_COOKIE_PATH)
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path(AUTH_COOKIE_PATH)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
