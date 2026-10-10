package com.org.flygo.dto;

import java.util.UUID;

public record UserResponse(

        UUID id,
        String fullName,
        String email,
        java.util.Set<UserRoles> role,
        OnBoardingState status

) {
}
