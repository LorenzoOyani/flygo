package com.org.flygo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NonNull;

public record SignUpRequest(String fullName, String email,@NotBlank @Size(min = 8, max = 72) String password) {

}
