package com.org.flygo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(@Schema(example = "Lawrence Jerumeh") String fullName,@Schema(example = "jerumeh@example.com") String email,@Schema(example = "SecurePass123")  @NotBlank @Size(min = 8, max = 72) String password) {

}
