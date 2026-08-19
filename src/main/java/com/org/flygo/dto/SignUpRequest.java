package com.org.flygo.dto;

import lombok.NonNull;

public record SignUpRequest(String fullName, String email, String password) {
}
