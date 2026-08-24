package com.org.flygo.service;

import com.org.flygo.dto.AuthResponse;
import com.org.flygo.dto.LoginRequest;
import com.org.flygo.dto.LoginResponse;
import com.org.flygo.dto.SignUpRequest;
import org.springframework.stereotype.Service;


public interface AuthService {

    AuthResponse register(SignUpRequest request);

    LoginResponse login(LoginRequest loginRequest);




}
