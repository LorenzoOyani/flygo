package com.org.flygo.dto;

import java.util.UUID;

public record UserResponse(

        UUID id,
        String fullName,
        String email,
        UserRoles role,
        OnBoardingState status

) {
}
