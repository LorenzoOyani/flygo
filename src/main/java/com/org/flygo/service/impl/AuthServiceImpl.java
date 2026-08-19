package com.org.flygo.service.impl;

import com.org.flygo.dto.AuthResponse;
import com.org.flygo.dto.SignUpRequest;
import com.org.flygo.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public AuthResponse register(SignUpRequest request) {
        return null;
    }
}
