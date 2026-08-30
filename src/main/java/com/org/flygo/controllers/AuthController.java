package com.org.flygo.controllers;

import com.org.flygo.dto.AuthResponse;
import com.org.flygo.dto.LoginRequest;
import com.org.flygo.dto.LoginResponse;
import com.org.flygo.dto.SignUpRequest;
import com.org.flygo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> authSignup(@Valid @RequestBody SignUpRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){

            LoginResponse loginResponse = authService.login(request);

            return ResponseEntity.ok(loginResponse);
        }
}
