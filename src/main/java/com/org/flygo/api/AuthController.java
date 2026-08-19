package com.org.flygo.api;

import com.org.flygo.dto.AuthResponse;
import com.org.flygo.dto.SignUpRequest;
import com.org.flygo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/vi/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> authSignup(@RequestBody SignUpRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok().build();
    }
}
