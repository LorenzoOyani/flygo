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

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Signup, login, token refresh and logout endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @SecurityRequirements

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> authSignup(@Valid @RequestBody SignUpRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Log in with email and password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @SecurityRequirements

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){

            LoginResponse loginResponse = authService.login(request);

            return ResponseEntity.ok(loginResponse);
        }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue("refreshToken") String rawRefreshToken,
            HttpServletResponse response) {

        authService.logout(rawRefreshToken);

        ResponseCookie clearedCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearedCookie.toString());

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @CookieValue("refreshToken")  RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.refreshToken()));
    }
}
